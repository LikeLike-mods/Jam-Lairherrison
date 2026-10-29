package net.ussm.the_shell.entites;

import java.util.EnumSet;
import java.util.List;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxRig.Section;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.ussm.the_shell.entites.ai.CetaceanBreatheGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.ussm.the_shell.entites.ai.CetaceanCruiseGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entites.init.CetaceanEntity;
import net.ussm.the_shell.entites.animations.SwimmingTurnProfiles;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class BasilosaurusEntity extends CetaceanEntity implements Enemy {
    private static final List<Section> HITBOXES = List.of(
            Section.of("head", "neck", "head", "jaw"),
            Section.of("body", "body", "abdomen", "pelvis"),
            Section.of("tail_base", "tail1"),
            Section.of("tail_middle", "tail2"),
            Section.of("tail_tip", "tail3", "right_fluke", "left_fluke")
    );

    public static final float SIZE_MULTIPLIER = 3.0F;
    public static final int BITE_DURATION = 18;
    public static final int BITE_HIT_TICK = 8;
    private static final double BITE_REACH = 2.5 * SIZE_MULTIPLIER;
    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(BasilosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HUNTING =
            SynchedEntityData.defineId(BasilosaurusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.basilosaurus.idle");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.basilosaurus.swim");
    private static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("animation.basilosaurus.swim_fast");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.basilosaurus.bite");
    private int biteTick;
    private int biteCooldown;
    @Nullable private LivingEntity biteTarget;

    public BasilosaurusEntity(EntityType<? extends BasilosaurusEntity> type, Level level) {
        super(type, level);
        xpReward = 20;
    }

    @Override public String getModelName() { return "basilosaurus"; }
    @Override public float getModelScale() { return SIZE_MULTIPLIER; }
    @Override public double getModelYOffset() { return -0.75; }
    @Override public TurnBendProfile getTurnProfile() { return SwimmingTurnProfiles.BASILOSAURUS; }
    @Override public boolean isPlayingSpecialPose() { return isBiting(); }
    @Override public String getHitboxAnimation() {
        return "animation.basilosaurus." + (isBiting() ? "bite" : getDeltaMovement().lengthSqr() > 0.000025
                ? entityData.get(HUNTING) ? "swim_fast" : "swim" : "idle");
    }

    @Override public List<Section> getHitboxSections() { return HITBOXES; }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.45)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BITING, false);
        builder.define(HUNTING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new CetaceanBreatheGoal(this));
        goalSelector.addGoal(1, new TryFindWaterGoal(this));
        goalSelector.addGoal(2, new BiteGoal());
        goalSelector.addGoal(4, new CetaceanCruiseGoal(this, 1.0, 12, 24));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractFish.class, true));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Squid.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        entityData.set(HUNTING, getTarget() != null && getTarget().isAlive());
        if (biteCooldown > 0) biteCooldown--;
        if (!isAlive()) {
            cancelBite();
        } else if (isBiting()) {
            biteTick++;
            if (biteTick == BITE_HIT_TICK && biteTarget != null && canBite(biteTarget)) {
                doHurtTarget(biteTarget);
            }
            if (biteTick >= BITE_DURATION) cancelBite();
        }
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    public boolean isBiting() {
        return entityData.get(BITING);
    }

    private boolean canBite(LivingEntity target) {
        if (!target.isAlive() || distanceToSqr(target) > BITE_REACH * BITE_REACH
                || !getSensing().hasLineOfSight(target)) return false;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        if (dx * dx + dz * dz < 1.0E-6) return true;
        float yaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        return Math.abs(Mth.wrapDegrees(yaw - yBodyRot)) <= 45.0F;
    }

    private void cancelBite() {
        entityData.set(BITING, false);
        biteTarget = null;
        biteTick = 0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 3, state -> {
            if (isBiting()) return state.setAndContinue(BITE);
            if (state.isMoving()) return state.setAndContinue(entityData.get(HUNTING) ? SWIM_FAST : SWIM);
            return state.setAndContinue(IDLE);
        }));
    }

    private class BiteGoal extends Goal {
        BiteGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return getTarget() != null && getTarget().isAlive() && isInWaterOrBubble();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (distanceToSqr(target) <= BITE_REACH * BITE_REACH) {
                getNavigation().stop();
                setYRot(Mth.approachDegrees(getYRot(), yHeadRot, MAX_TURN_PER_TICK));
                setYBodyRot(getYRot());
                if (!isBiting() && biteCooldown == 0 && canBite(target)) {
                    biteTarget = target;
                    biteTick = 0;
                    biteCooldown = 35;
                    startHitboxAnimation();
                    entityData.set(BITING, true);
                    playSound(SoundEvents.DOLPHIN_ATTACK, 1.2F, 0.6F);
                }
            } else if (!isBiting()) {
                getNavigation().moveTo(target, 1.4);
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
            cancelBite();
        }
    }
}
