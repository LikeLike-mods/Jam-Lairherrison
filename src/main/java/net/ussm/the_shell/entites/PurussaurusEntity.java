package net.ussm.the_shell.entites;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.ussm.the_shell.entites.ai.ReptileMoveControl;
import net.ussm.the_shell.entites.animations.SwimmingTurnProfiles;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import net.ussm.the_shell.entites.animations.TurnBendState;
import net.ussm.the_shell.entites.hitbox.AnimatedHitbox;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxEntity;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxPart;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxRig.Section;
import net.ussm.the_shell.init.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PurussaurusEntity extends Monster implements GeoEntity, AnimatedHitboxEntity {
    private static final List<Section> HITBOXES = List.of(
            Section.of("head", "neck", "head", "jaw"),
            Section.of("chest", "chest", "right_front_upper", "right_front_lower", "right_front_foot",
                    "left_front_upper", "left_front_lower", "left_front_foot"),
            Section.of("body", "body", "right_hind_upper", "right_hind_lower", "right_hind_foot",
                    "left_hind_upper", "left_hind_lower", "left_hind_foot"),
            Section.of("tail_base", "tail1", "tail2"),
            Section.of("tail_middle", "tail3", "tail4"),
            Section.of("tail_tip", "tail5", "tail6", "tail7")
    );

    public static final int MAX_TURN_PER_TICK = 4;
    private static final int NONE = 0;
    private static final int BITE = 1;
    private static final int THREAT = 2;
    private static final int ROLL = 3;
    private static final int BITE_DURATION = 20;
    private static final int BITE_HIT_TICK = 8;
    private static final int THREAT_DURATION = 60;
    private static final int ROLL_DURATION = 50;
    private static final int[] ROLL_HIT_TICKS = {15, 27, 39};
    private static final double BITE_REACH = 6.8;
    private static final double MOUTH_DISTANCE = 6.0;
    private static final double AMBUSH_RANGE = 12.0;
    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(PurussaurusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> POSE_START =
            SynchedEntityData.defineId(PurussaurusEntity.class, EntityDataSerializers.LONG);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.purussaurus.idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation.purussaurus.walk");
    private static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("animation.purussaurus.swim");
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("animation.purussaurus.bite");
    private static final RawAnimation THREAT_ANIM = RawAnimation.begin().thenPlay("animation.purussaurus.threat");
    private static final RawAnimation ROLL_ANIM = RawAnimation.begin().thenPlay("animation.purussaurus.death_roll");

    private final AnimatedHitbox<PurussaurusEntity> hitbox;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final TurnBendState turnState = new TurnBendState(MAX_TURN_PER_TICK);
    private int actionTick;
    private int attackCooldown;
    private boolean threatened;
    @Nullable private LivingEntity prey;

    public PurussaurusEntity(EntityType<? extends PurussaurusEntity> type, Level level) {
        super(type, level);
        moveControl = new ReptileMoveControl(this, 3.5F, 4.5F, 0.12F, 0.02F);
        setPathfindingMalus(PathType.WATER, 0.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        xpReward = 30;
        hitbox = new AnimatedHitbox<>(this);
        setId(ENTITY_COUNTER.getAndAdd(hitbox.parts().length + 1) + 1);
    }

    @Override public String getModelName() { return "purussaurus"; }
    @Override public float getModelScale() { return 1.0F; }
    @Override public double getModelYOffset() { return 0; }
    @Override public TurnBendProfile getTurnProfile() { return SwimmingTurnProfiles.PURUSSAURUS; }
    @Override public List<Section> getHitboxSections() { return HITBOXES; }
    @Override public boolean isPlayingSpecialPose() { return action() != NONE; }
    @Override public boolean isTurnBendingAllowed() { return action() != ROLL; }

    @Override public String getHitboxAnimation() {
        return "animation.purussaurus." + switch (action()) {
            case BITE -> "bite";
            case THREAT -> "threat";
            case ROLL -> "death_roll";
            default -> getDeltaMovement().horizontalDistanceSqr() > 0.000025 ? paddling() ? "swim" : "walk" : "idle";
        };
    }

    @Override public double getHitboxAnimationTick(float partialTick) {
        return isPlayingSpecialPose() ? Math.max(0, level().getGameTime() - entityData.get(POSE_START) + partialTick)
                : tickCount + partialTick;
    }

    @Override public boolean isMultipartEntity() { return true; }
    @Override public AnimatedHitboxPart[] getParts() { return hitbox == null ? null : hitbox.parts(); }

    @Override protected void pushEntities() {
        super.pushEntities();
        AnimatedHitboxPart.pushAway(this, getParts());
    }

    @Override public void setId(int id) {
        super.setId(id);
        if (hitbox != null) hitbox.setId(id);
    }

    @Override public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (hitbox != null) updateHitParts(1);
    }

    public void updateHitParts(float partialTick) {
        if (hitbox != null) hitbox.update(partialTick);
    }

    @Override public AABB getBoundingBoxForCulling() {
        return hitbox.cullingBounds(super.getBoundingBoxForCulling());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, NONE);
        builder.define(POSE_START, 0L);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new AmbushGoal());
        goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 120));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0, 240));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> distanceToSqr(player) < AMBUSH_RANGE * AMBUSH_RANGE));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public void travel(Vec3 input) {
        if (isControlledByLocalInstance() && isInWater()) {
            moveRelative(getSpeed(), new Vec3(input.x, 0, input.z));
            Vec3 motion = getDeltaMovement();
            double rise = Mth.clamp((waterSurface() - getBbHeight() * 0.65 - getY()) * 0.1, -0.04, 0.03);
            setDeltaMovement(motion.x, Mth.lerp(0.3, motion.y, rise), motion.z);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().multiply(0.9, 0.8, 0.9));
        } else {
            super.travel(input);
        }
    }

    private double waterSurface() {
        BlockPos.MutableBlockPos pos = blockPosition().mutable();
        int top = level().getMaxBuildHeight();
        while (pos.getY() < top && level().getFluidState(pos.above()).is(FluidTags.WATER)) pos.move(0, 1, 0);
        return pos.getY() + level().getFluidState(pos).getHeight(level(), pos);
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public int getMaxHeadYRot() {
        return 25;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void tick() {
        hitbox.beginTick();
        super.tick();
        turnState.update(yBodyRot, yBodyRotO);
        if (!level().isClientSide) tickAction();
        updateHitParts(1);
    }

    private void tickAction() {
        if (attackCooldown > 0) attackCooldown--;
        if (getTarget() == null) threatened = false;
        int action = action();
        if (action == NONE) return;
        if (!isAlive()) {
            setAction(NONE);
            return;
        }
        actionTick++;
        if (action == BITE && actionTick == BITE_HIT_TICK && prey != null && canBite(prey)) {
            boolean hit = doHurtTarget(prey);
            if (hit && prey.isAlive() && prey.isInWater() && random.nextFloat() < 0.4F) {
                startAction(ROLL);
                playSound(SoundEvents.GENERIC_SPLASH, 2.0F, 0.6F);
                return;
            }
        }
        if (action == ROLL) holdPrey();
        int duration = switch (action) {
            case BITE -> BITE_DURATION;
            case THREAT -> THREAT_DURATION;
            default -> ROLL_DURATION;
        };
        if (actionTick >= duration) setAction(NONE);
    }

    private void holdPrey() {
        if (prey == null || !prey.isAlive()) return;
        Vec3 mouth = position().add(Vec3.directionFromRotation(0, yBodyRot).scale(MOUTH_DISTANCE)).add(0, 0.4, 0);
        prey.setDeltaMovement(mouth.subtract(prey.position()).scale(0.5));
        prey.hurtMarked = true;
        for (int hit : ROLL_HIT_TICKS) {
            if (actionTick == hit) {
                prey.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F);
            }
        }
    }

    private boolean paddling() {
        return isInWater() && !onGround();
    }

    private int action() {
        return entityData.get(ACTION);
    }

    private void setAction(int action) {
        entityData.set(ACTION, action);
        actionTick = 0;
        if (action == NONE) prey = null;
    }

    private void startAction(int action) {
        entityData.set(POSE_START, level().getGameTime());
        setAction(action);
    }

    private boolean canBite(LivingEntity target) {
        if (!target.isAlive() || distanceToSqr(target) > BITE_REACH * BITE_REACH) return false;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        if (dx * dx + dz * dz < 1.0E-6) return true;
        float yaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        return Math.abs(Mth.wrapDegrees(yaw - yBodyRot)) <= 40.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.mobSound(this, "ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.mobSound(this, "death");
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.8F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state -> switch (action()) {
            case BITE -> state.setAndContinue(BITE_ANIM);
            case THREAT -> state.setAndContinue(THREAT_ANIM);
            case ROLL -> state.setAndContinue(ROLL_ANIM);
            default -> state.setAndContinue(state.getLimbSwingAmount() > 0.05F ? paddling() ? SWIM_ANIM : WALK_ANIM : IDLE_ANIM);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public float getTurnBend(float partialTick) {
        return turnState.getBend(partialTick);
    }

    private final class AmbushGoal extends Goal {
        private int repath;

        AmbushGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || action() != NONE;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (action() != NONE) {
                getNavigation().stop();
                if (target != null && action() != ROLL) getLookControl().setLookAt(target, 30.0F, 30.0F);
                return;
            }
            if (target == null) return;
            getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (!threatened) {
                threatened = true;
                getNavigation().stop();
                startAction(THREAT);
                playSound(SoundEvents.CAT_HISS, 2.5F, 0.35F);
                return;
            }
            if (canBite(target)) {
                getNavigation().stop();
                if (attackCooldown == 0) {
                    prey = target;
                    attackCooldown = 40;
                    startAction(BITE);
                    playSound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.7F);
                }
            } else if (--repath <= 0 || getNavigation().isDone()) {
                repath = 10;
                getNavigation().moveTo(target, 1.8);
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }
}
