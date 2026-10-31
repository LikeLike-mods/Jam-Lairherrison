package net.ussm.the_shell.entites;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.ussm.the_shell.entites.ai.ReptileMoveControl;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxPart;
import net.ussm.the_shell.init.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TitanoboaEntity extends Monster implements GeoEntity {
    public static final int SEGMENTS = 21;
    public static final double SEGMENT_LENGTH = 0.625;
    private static final double STEP = 0.2;
    private static final int TRAIL = 100;
    private static final double WAVE = Math.PI * 2 / 6.5;
    private static final double SWAY = 0.4;
    private static final int[] PART_JOINTS = {2, 5, 8, 11, 14, 17, 20};
    private static final int BITE_DURATION = 15;
    private static final int BITE_HIT_TICK = 6;
    private static final double BITE_REACH = 2.6;
    private static final double AMBUSH_RANGE = 10.0;
    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(TitanoboaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.titanoboa.idle");
    private static final RawAnimation BITE_ANIM = RawAnimation.begin().thenPlay("animation.titanoboa.bite");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AnimatedHitboxPart[] parts = new AnimatedHitboxPart[PART_JOINTS.length];
    private final double[] trail = new double[TRAIL * 3];
    private int newest = -1;
    private int trailSize;
    private double travelled;
    private double smoothY;
    private final double[] path = new double[(SEGMENTS + 1) * 3];
    private final double[] joints = new double[(SEGMENTS + 1) * 3];
    private final double[] jointsO = new double[(SEGMENTS + 1) * 3];
    private final double[] jointsLerp = new double[(SEGMENTS + 1) * 3];
    private boolean jointsReady;
    private int lerpTick = Integer.MIN_VALUE;
    private float lerpPartial = Float.NaN;
    private int biteTick;
    private int biteCooldown;
    @Nullable private LivingEntity biteTarget;

    public TitanoboaEntity(EntityType<? extends TitanoboaEntity> type, Level level) {
        super(type, level);
        moveControl = new ReptileMoveControl(this, 6.0F, 7.0F, 0.10F, 0.025F);
        setPathfindingMalus(PathType.WATER, 0.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        xpReward = 25;
        for (int i = 0; i < parts.length; i++) parts[i] = new AnimatedHitboxPart(this, "segment" + i);
        setId(ENTITY_COUNTER.getAndAdd(parts.length + 1) + 1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.STEP_HEIGHT, 1.1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BITING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new StrikeGoal());
        goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 160));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0, 200));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> distanceToSqr(player) < AMBUSH_RANGE * AMBUSH_RANGE));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override public boolean isMultipartEntity() { return true; }
    @Override public AnimatedHitboxPart[] getParts() { return parts; }

    @Override
    protected void pushEntities() {
        super.pushEntities();
        AnimatedHitboxPart.pushAway(this, parts);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < parts.length; i++) if (parts[i] != null) parts[i].setId(id + i + 1);
    }

    @Override
    public void travel(Vec3 input) {
        if (isControlledByLocalInstance() && isInWater()) {
            moveRelative(getSpeed(), new Vec3(input.x, 0, input.z));
            Vec3 motion = getDeltaMovement();
            double rise = Mth.clamp((waterSurface() - getBbHeight() * 0.55 - getY()) * 0.1, -0.04, 0.03);
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
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void tick() {
        for (AnimatedHitboxPart part : parts) part.beginTick();
        super.tick();
        updateBody();
        updateParts();
        if (!level().isClientSide) tickBite();
    }

    private void updateBody() {
        boolean reset = updateTrail();
        if (jointsReady && !reset) System.arraycopy(joints, 0, jointsO, 0, joints.length);
        computeJoints();
        if (!jointsReady || reset) System.arraycopy(joints, 0, jointsO, 0, joints.length);
        jointsReady = true;
        lerpTick = Integer.MIN_VALUE;
    }

    private boolean updateTrail() {
        double dx = getX() - xo;
        double dz = getZ() - zo;
        double moved = Math.sqrt(dx * dx + dz * dz);
        if (trailSize == 0 || moved > 4 || Math.abs(getY() - smoothY) > 3) {
            trailSize = 0;
            smoothY = getY();
            float yaw = getYRot() * Mth.DEG_TO_RAD;
            double hx = -Mth.sin(yaw);
            double hz = Mth.cos(yaw);
            for (int i = TRAIL - 1; i >= 0; i--) addTrailPoint(getX() - hx * STEP * i, smoothY, getZ() - hz * STEP * i);
            return true;
        }
        travelled += moved;
        smoothY += (getY() - smoothY) * 0.3;
        int at = newest * 3;
        double ddx = getX() - trail[at];
        double ddy = smoothY - trail[at + 1];
        double ddz = getZ() - trail[at + 2];
        if (ddx * ddx + ddy * ddy + ddz * ddz >= STEP * STEP) addTrailPoint(getX(), smoothY, getZ());
        return false;
    }

    private void addTrailPoint(double x, double y, double z) {
        newest = (newest + 1) % TRAIL;
        trail[newest * 3] = x;
        trail[newest * 3 + 1] = y;
        trail[newest * 3 + 2] = z;
        trailSize = Math.min(trailSize + 1, TRAIL);
    }

    private void computeJoints() {
        double px = getX();
        double py = smoothY;
        double pz = getZ();
        path[0] = px;
        path[1] = py;
        path[2] = pz;
        int index = 1;
        double reached = 0;
        double target = SEGMENT_LENGTH;
        float yaw = getYRot() * Mth.DEG_TO_RAD;
        double dirX = Mth.sin(yaw);
        double dirY = 0;
        double dirZ = -Mth.cos(yaw);
        for (int k = 0; k < trailSize && index <= SEGMENTS; k++) {
            int at = ((newest - k + TRAIL) % TRAIL) * 3;
            double lx = trail[at] - px;
            double ly = trail[at + 1] - py;
            double lz = trail[at + 2] - pz;
            double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
            if (len < 1.0E-6) continue;
            while (index <= SEGMENTS && reached + len >= target) {
                double f = (target - reached) / len;
                path[index * 3] = px + lx * f;
                path[index * 3 + 1] = py + ly * f;
                path[index * 3 + 2] = pz + lz * f;
                index++;
                target += SEGMENT_LENGTH;
            }
            dirX = lx / len;
            dirY = ly / len;
            dirZ = lz / len;
            reached += len;
            px = trail[at];
            py = trail[at + 1];
            pz = trail[at + 2];
        }
        for (; index <= SEGMENTS; index++) {
            double f = target - reached;
            path[index * 3] = px + dirX * f;
            path[index * 3 + 1] = py + dirY * f;
            path[index * 3 + 2] = pz + dirZ * f;
            target += SEGMENT_LENGTH;
        }
        for (int i = 0; i <= SEGMENTS; i++) {
            int a = Math.max(i - 1, 0) * 3;
            int b = Math.min(i + 1, SEGMENTS) * 3;
            double tx = path[a] - path[b];
            double tz = path[a + 2] - path[b + 2];
            double len = Math.sqrt(tx * tx + tz * tz);
            double envelope = Math.min(1.0, i / 4.0);
            double sway = len < 1.0E-6 ? 0 : SWAY * envelope * envelope * Math.sin((travelled - i * SEGMENT_LENGTH) * WAVE) / len;
            joints[i * 3] = path[i * 3] - tz * sway;
            joints[i * 3 + 1] = path[i * 3 + 1];
            joints[i * 3 + 2] = path[i * 3 + 2] + tx * sway;
        }
        for (int i = 1; i <= SEGMENTS; i++) {
            double lx = joints[i * 3] - joints[i * 3 - 3];
            double ly = joints[i * 3 + 1] - joints[i * 3 - 2];
            double lz = joints[i * 3 + 2] - joints[i * 3 - 1];
            double len = Math.sqrt(lx * lx + ly * ly + lz * lz);
            if (len < 1.0E-6) continue;
            double f = SEGMENT_LENGTH / len;
            joints[i * 3] = joints[i * 3 - 3] + lx * f;
            joints[i * 3 + 1] = joints[i * 3 - 2] + ly * f;
            joints[i * 3 + 2] = joints[i * 3 - 1] + lz * f;
        }
    }

    public double[] joints(float partialTick) {
        if (!jointsReady) updateBody();
        if (partialTick >= 1.0F) return joints;
        if (lerpTick == tickCount && lerpPartial == partialTick) return jointsLerp;
        lerpTick = tickCount;
        lerpPartial = partialTick;
        for (int i = 0; i < joints.length; i++) jointsLerp[i] = Mth.lerp(partialTick, jointsO[i], joints[i]);
        return jointsLerp;
    }

    public float visualYaw(float partialTick) {
        double[] j = joints(partialTick);
        double fx = j[0] - j[3];
        double fz = j[2] - j[5];
        if (fx * fx + fz * fz < 1.0E-6) return Mth.rotLerp(partialTick, yRotO, getYRot());
        return (float) (Mth.atan2(fz, fx) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private void updateParts() {
        double[] j = joints(1.0F);
        for (int i = 0; i < parts.length; i++) {
            int at = PART_JOINTS[i] * 3;
            double half = i < 4 ? 0.5 : i < 6 ? 0.4 : 0.3;
            double height = i < 4 ? 0.85 : i < 6 ? 0.65 : 0.45;
            parts[i].updateBounds(new AABB(j[at] - half, j[at + 1], j[at + 2] - half,
                    j[at] + half, j[at + 1] + height, j[at + 2] + half));
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB box = super.getBoundingBoxForCulling();
        double[] j = joints(1.0F);
        for (int i = 0; i <= SEGMENTS; i++) {
            box = box.minmax(new AABB(j[i * 3], j[i * 3 + 1], j[i * 3 + 2], j[i * 3], j[i * 3 + 1] + 1, j[i * 3 + 2]));
        }
        return box.inflate(1.0);
    }

    private void tickBite() {
        if (biteCooldown > 0) biteCooldown--;
        if (!isBiting()) return;
        if (!isAlive()) {
            stopBite();
            return;
        }
        biteTick++;
        if (biteTick == BITE_HIT_TICK && biteTarget != null && canBite(biteTarget) && doHurtTarget(biteTarget)) {
            biteTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
        }
        if (biteTick >= BITE_DURATION) stopBite();
    }

    private void stopBite() {
        entityData.set(BITING, false);
        biteTarget = null;
        biteTick = 0;
    }

    public boolean isBiting() {
        return entityData.get(BITING);
    }

    private boolean canBite(LivingEntity target) {
        if (!target.isAlive() || distanceToSqr(target) > BITE_REACH * BITE_REACH) return false;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        if (dx * dx + dz * dz < 1.0E-6) return true;
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        return Math.abs(Mth.wrapDegrees(yaw - getYRot())) <= 50.0F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 320;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.mobSound(this, "ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.mobSound(this, "hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.mobSound(this, "death");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state ->
                state.setAndContinue(isBiting() ? BITE_ANIM : IDLE_ANIM)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private final class StrikeGoal extends Goal {
        private int repath;

        StrikeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() || isBiting();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (isBiting() || target == null) {
                getNavigation().stop();
                return;
            }
            if (canBite(target)) {
                getNavigation().stop();
                if (biteCooldown == 0) {
                    biteTarget = target;
                    biteTick = 0;
                    biteCooldown = 30;
                    entityData.set(BITING, true);
                    playSound(SoundEvents.EVOKER_FANGS_ATTACK, 1.2F, 0.6F);
                }
            } else if (--repath <= 0 || getNavigation().isDone()) {
                repath = 10;
                getNavigation().moveTo(target, 1.6);
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }
    }
}
