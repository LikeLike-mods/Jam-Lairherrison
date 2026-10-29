package net.ussm.theshell.entity;

import java.util.EnumSet;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.ussm.theshell.entity.ai.LeviathanBiteCycle;
import net.ussm.theshell.entity.animations.LeviathanTurnState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LeviathanEntity extends Monster implements GeoEntity {
    public static final float SIZE_MULTIPLIER = 3.0F;
    public static final int MAX_TURN_PER_TICK = 5;

    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(LeviathanEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.leviathan.idle");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.leviathan.swim");
    private static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.leviathan.bite");
    private static final double BITE_START_RANGE = 3.6 * SIZE_MULTIPLIER;
    private static final double BITE_HIT_RANGE = 4.0 * SIZE_MULTIPLIER;
    private static final float BITE_HALF_ANGLE = 45.0F;

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final LeviathanBiteCycle biteCycle = new LeviathanBiteCycle();
    private final LeviathanTurnState turnState = new LeviathanTurnState(MAX_TURN_PER_TICK);
    @Nullable
    private LivingEntity biteTarget;

    public LeviathanEntity(EntityType<? extends LeviathanEntity> entityType, Level level) {
        super(entityType, level);
        moveControl = new SmoothSwimmingMoveControl(this, 35, MAX_TURN_PER_TICK, 0.04F, 0.1F, false);
        lookControl = new SmoothSwimmingLookControl(this, 10);
        setPathfindingMalus(PathType.WATER, 0.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        xpReward = 100;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.ATTACK_KNOCKBACK, 3.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BITING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new TryFindWaterGoal(this));
        goalSelector.addGoal(1, new BiteGoal());
        goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 30));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getFluidState(pos).is(FluidTags.WATER) ? 10.0F : super.getWalkTargetValue(pos, level);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(getSpeed(), travelVector);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            if (getTarget() == null) {
                setDeltaMovement(getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isAlive() && !isInWater() && onGround()) {
            setDeltaMovement(getDeltaMovement().add(
                    (random.nextFloat() * 2.0 - 1.0) * 0.2, 0.4,
                    (random.nextFloat() * 2.0 - 1.0) * 0.2));
            setOnGround(false);
            hasImpulse = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            turnState.update(yBodyRot, yBodyRotO);
        } else {
            if (!isAlive()) {
                cancelBite();
            } else {
                if (biteCycle.tick() && biteTarget != null && biteTarget.isAlive()
                        && distanceToSqr(biteTarget) <= BITE_HIT_RANGE * BITE_HIT_RANGE
                        && isFacingBiteTarget(biteTarget)
                        && getSensing().hasLineOfSight(biteTarget)) {
                    doHurtTarget(biteTarget);
                }
                if (!biteCycle.isActive()) {
                    entityData.set(BITING, false);
                    biteTarget = null;
                }
            }
        }
    }

    @Override
    protected float tickHeadTurn(float bodyYaw, float animStep) {
        float result = super.tickHeadTurn(bodyYaw, animStep);
        yBodyRot = Mth.approachDegrees(yBodyRotO, yBodyRot, MAX_TURN_PER_TICK);
        if (!level().isClientSide) {
            setYRot(yBodyRot);
        }
        return result;
    }

    public float getTurnBend(float partialTick) {
        return turnState.getBend(partialTick);
    }

    private boolean isFacingBiteTarget(LivingEntity target) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        if (dx * dx + dz * dz < 1.0E-6) {
            return true;
        }

        float targetYaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        return Math.abs(Mth.wrapDegrees(targetYaw - yBodyRot)) <= BITE_HALF_ANGLE;
    }

    private void cancelBite() {
        biteCycle.cancel();
        entityData.set(BITING, false);
        biteTarget = null;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_AMBIENT : SoundEvents.ELDER_GUARDIAN_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_HURT : SoundEvents.ELDER_GUARDIAN_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isInWaterOrBubble() ? SoundEvents.ELDER_GUARDIAN_DEATH : SoundEvents.ELDER_GUARDIAN_DEATH_LAND;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 0, state -> {
            if (entityData.get(BITING)) {
                return state.setAndContinue(BITE);
            }
            return state.setAndContinue(isInWater() && state.isMoving() ? SWIM : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    private final class BiteGoal extends Goal {
        private int pathUpdateTicks;

        private BiteGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            pathUpdateTicks = 0;
        }

        @Override
        public void stop() {
            getNavigation().stop();
            cancelBite();
        }

        @Override
        public void tick() {
            LivingEntity target = biteCycle.isActive() ? biteTarget : getTarget();
            if (target == null) {
                return;
            }
            getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (biteCycle.isActive()) {
                stopAndFaceTarget();
                return;
            }

            if (distanceToSqr(target) <= BITE_START_RANGE * BITE_START_RANGE
                    && getSensing().hasLineOfSight(target)) {
                stopAndFaceTarget();
                if (isFacingBiteTarget(target) && biteCycle.start()) {
                    biteTarget = target;
                    entityData.set(BITING, true);
                    playSound(SoundEvents.RAVAGER_ATTACK, 1.5F, 0.65F);
                }
                return;
            }

            if (--pathUpdateTicks <= 0) {
                pathUpdateTicks = 5;
                getNavigation().moveTo(target, 1.25);
            }
        }

        private void stopAndFaceTarget() {
            getNavigation().stop();
            setYRot(Mth.approachDegrees(getYRot(), yHeadRot, MAX_TURN_PER_TICK));
            setYBodyRot(getYRot());
        }
    }
}
