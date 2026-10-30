package net.ussm.the_shell.entites.init;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.ussm.the_shell.entites.hitbox.AnimatedHitbox;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxEntity;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxPart;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.LookControl;
import net.ussm.the_shell.entites.ai.CetaceanMoveControl;
import net.ussm.the_shell.init.ModSounds;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import javax.annotation.Nullable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.ussm.the_shell.entites.ai.CetaceanSwimNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.animations.TurnBendState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class CetaceanEntity extends WaterAnimal implements GeoEntity, AnimatedHitboxEntity {
    public static final int MAX_TURN_PER_TICK = 4;
    private static final EntityDataAccessor<Long> POSE_START = SynchedEntityData.defineId(CetaceanEntity.class, EntityDataSerializers.LONG);
    private final AnimatedHitbox<CetaceanEntity> hitbox;
    private final TurnBendState turnState = new TurnBendState(getMaxTurnPerTick(), getTurnSmoothing());
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    protected CetaceanEntity(EntityType<? extends CetaceanEntity> type, Level level) {
        super(type, level);
        moveControl = new CetaceanMoveControl(this);
        lookControl = new LookControl(this) {
            @Override
            protected boolean resetXRotOnTick() {
                return false;
            }
        };
        setAirSupply(getMaxAirSupply());
        hitbox = new AnimatedHitbox<>(this);
        setId(ENTITY_COUNTER.getAndAdd(hitbox.parts().length + 1) + 1);
    }

    public abstract String getModelName();
    public abstract float getModelScale();
    public abstract double getModelYOffset();
    public abstract TurnBendProfile getTurnProfile();
    public abstract String getHitboxAnimation();
    @Override public boolean rotatesModelWithPitch() { return true; }

    public double getHitboxAnimationTick(float partialTick) {
        return isPlayingSpecialPose() ? Math.max(0, level().getGameTime() - entityData.get(POSE_START) + partialTick) : tickCount + partialTick;
    }

    protected void startHitboxAnimation() { entityData.set(POSE_START, level().getGameTime()); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POSE_START, 0L);
    }

    @Override public boolean isMultipartEntity() { return true; }
    @Override public AnimatedHitboxPart[] getParts() { return hitbox == null ? null : hitbox.parts(); }

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

    public float getMaxTurnPerTick() { return MAX_TURN_PER_TICK; }
    public float getTurnAcceleration() { return MAX_TURN_PER_TICK; }
    public float getPitchTurnPerTick() { return 5; }
    public float getTurnSmoothing() { return 0.2F; }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level) {
            @Override
            protected PathFinder createPathFinder(int maxVisitedNodes) {
                nodeEvaluator = new CetaceanSwimNodeEvaluator();
                return new PathFinder(nodeEvaluator, maxVisitedNodes);
            }

            @Override
            protected Vec3 getTempMobPos() {
                return mob.position();
            }

            @Override
            protected boolean canUpdatePath() {
                return isInWaterOrBubble() || !onGround();
            }
        };
    }

    @Override
    protected float tickHeadTurn(float bodyYaw, float animStep) {
        yBodyRot = Mth.approachDegrees(yBodyRotO, getYRot(), getMaxTurnPerTick());
        return animStep;
    }

    @Override
    public void tick() {
        hitbox.beginTick();
        super.tick();
        turnState.update(yBodyRot, yBodyRotO);
        updateHitParts(1);
    }

    @Override
    public float getTurnBend(float partialTick) {
        return turnState.getBend(partialTick);
    }

    @Override
    public void travel(Vec3 input) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(getSpeed(), input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            calculateEntityAnimation(true);
        } else {
            super.travel(input);
        }
    }

    public void swimTo(Vec3 destination, double speed) {
        if (level().noCollision(this, getBoundingBox().expandTowards(destination.subtract(position())))) {
            getNavigation().stop();
            getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, speed);
        } else if (getNavigation().isDone() || tickCount % 20 == 0) {
            getNavigation().moveTo(destination.x, destination.y, destination.z, 0, speed);
        }
    }

    @Override
    public int getMaxAirSupply() {
        return 4800;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return getMaxAirSupply();
    }

    @Override
    protected void handleAirSupply(int previousAir) {
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return hitbox.cullingBounds(super.getBoundingBoxForCulling());
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.mobSound(this, "ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        SoundEvent death = ModSounds.mobSound(this, "death");
        return death != null ? death : SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.55F;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
