package net.ussm.the_shell.entites;

import java.util.EnumSet;
import java.util.List;
import net.ussm.the_shell.entites.hitbox.AnimatedHitboxRig.Section;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.ussm.the_shell.entites.ai.CetaceanBreatheGoal;
import net.ussm.the_shell.entites.ai.WhaleBreachWitness;
import net.minecraft.world.entity.ai.goal.Goal;
import net.ussm.the_shell.entites.ai.CetaceanCruiseGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.init.CetaceanEntity;
import net.ussm.the_shell.entites.animations.SwimmingTurnProfiles;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import net.ussm.the_shell.world.water.WaveImpacts;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class BlueWhaleEntity extends CetaceanEntity {
    private static final List<Section> HITBOXES = List.of(
            Section.of("head", "head", "jaw"),
            Section.of("body", "body"),
            Section.of("tail_base", "tail1", "tail2"),
            Section.of("tail_middle", "tail3"),
            Section.of("tail_tip", "tail4", "right_fluke", "left_fluke")
    );

    public static final float SIZE_MULTIPLIER = 1.0F;
    public static final float GEOMETRY_SCALE = 4.0F;
    private static final double SURFACE_OFFSET = 3.0;
    private static final int SURFACE_CLEARANCE_RADIUS = 16;
    private static final int BREACH_CLEARANCE_HEIGHT = 24;
    public static final int BREACH_DURATION = 280;
    public static final int BREACH_IMPACT_TICK = 130;
    public static final int FEED_DURATION = 400;
    private static final int NONE = 0;
    private static final int BREACHING = 1;
    private static final int FEEDING = 2;
    private static final EntityDataAccessor<Integer> ACTIVITY =
            SynchedEntityData.defineId(BlueWhaleEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.blue_whale.idle");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.blue_whale.swim");
    private static final RawAnimation BREACH = RawAnimation.begin().thenPlay("animation.blue_whale.breach");
    private static final RawAnimation FEED = RawAnimation.begin().thenPlay("animation.blue_whale.feed");
    private int activityTick;
    private int activityCooldown = 200;
    private int nextSurfaceSearchTick;
    private boolean nextActivityIsBreach = true;
    @Nullable private Vec3 activitySurface;

    public BlueWhaleEntity(EntityType<? extends BlueWhaleEntity> type, Level level) {
        super(type, level);
        xpReward = 10;
    }

    @Override public String getModelName() { return "blue_whale"; }
    @Override public float getModelScale() { return SIZE_MULTIPLIER; }
    @Override public double getModelYOffset() { return 1.625; }
    @Override public TurnBendProfile getTurnProfile() { return SwimmingTurnProfiles.BLUE_WHALE; }
    @Override public boolean isPlayingSpecialPose() { return isPerformingSurfaceActivity(); }
    @Override public String getHitboxAnimation() {
        return "animation.blue_whale." + switch (entityData.get(ACTIVITY)) {
            case BREACHING -> "breach";
            case FEEDING -> "feed";
            default -> getDeltaMovement().lengthSqr() > 0.000025 ? "swim" : "idle";
        };
    }
    public boolean isBreaching() { return entityData.get(ACTIVITY) == BREACHING; }
    public int getActivityTick() { return activityTick; }
    public double getBreachSurfaceY() { return activitySurface == null ? getY() + SURFACE_OFFSET : activitySurface.y; }

    @Override public float getMaxTurnPerTick() { return 1.1F; }
    @Override public float getTurnAcceleration() { return 0.12F; }
    @Override public float getPitchTurnPerTick() { return 1.0F; }
    @Override public float getTurnSmoothing() { return 0.1F; }

    @Override public List<Section> getHitboxSections() { return HITBOXES; }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVITY, NONE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new CetaceanBreatheGoal(this));
        goalSelector.addGoal(1, new TryFindWaterGoal(this));
        goalSelector.addGoal(2, new SurfaceActivityGoal());
        goalSelector.addGoal(4, new CetaceanCruiseGoal(this, 1.0, 24, 40));
    }

    public boolean isPerformingSurfaceActivity() {
        return entityData.get(ACTIVITY) != NONE;
    }

    @Override
    public boolean isTurnBendingAllowed() {
        return !isPerformingSurfaceActivity();
    }

    @Override
    public void travel(Vec3 input) {
        if (isPerformingSurfaceActivity() && isInWater()) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.travel(input);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (activityCooldown > 0) activityCooldown--;
        if (!isPerformingSurfaceActivity()) return;
        if (!isAlive() || !isInWater() || getAirSupply() < 200) {
            stopSurfaceActivity();
            return;
        }
        activityTick++;
        WhaleBreachWitness.tick(this);
        if (entityData.get(ACTIVITY) == BREACHING && activityTick == BREACH_IMPACT_TICK
                && activitySurface != null && level() instanceof ServerLevel server) {
            WaveImpacts.whaleSplash(server, activitySurface);
            server.sendParticles(ParticleTypes.SPLASH, activitySurface.x, activitySurface.y, activitySurface.z,
                    90, 2.5 * GEOMETRY_SCALE, 0.3, 2.5 * GEOMETRY_SCALE, 0.25);
            playSound(SoundEvents.GENERIC_SPLASH, 3.0F, 0.55F);
        }
        int duration = entityData.get(ACTIVITY) == BREACHING ? BREACH_DURATION : FEED_DURATION;
        if (activityTick >= duration) stopSurfaceActivity();
    }

    public boolean tryStartSurfaceActivity(boolean breach) {
        if (level().isClientSide || !isAlive() || !isInWater() || isPerformingSurfaceActivity()
                || getAirSupply() < 1200) return false;
        Vec3 surface = findSurface();
        if (surface == null || Math.abs(getY() - (surface.y - SURFACE_OFFSET)) > 0.65) return false;
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        setXRot(0.0F);
        activitySurface = surface;
        activityTick = 0;
        startHitboxAnimation();
        entityData.set(ACTIVITY, breach ? BREACHING : FEEDING);
        return true;
    }

    private void stopSurfaceActivity() {
        entityData.set(ACTIVITY, NONE);
        activitySurface = null;
        activityTick = 0;
        activityCooldown = 400 + random.nextInt(401);
    }

    @Nullable
    private Vec3 findSurface() {
        BlockPos.MutableBlockPos pos = blockPosition().mutable();
        for (int y = getBlockY(); y <= level().getMaxBuildHeight() - BREACH_CLEARANCE_HEIGHT - 1; y++) {
            pos.setY(y);
            if (!level().hasChunkAt(pos)) return null;
            if (level().getFluidState(pos).is(FluidTags.WATER)) continue;
            if (!level().getBlockState(pos).isAir() || !level().getFluidState(pos.below()).is(FluidTags.WATER)) return null;
            for (int dx = -SURFACE_CLEARANCE_RADIUS; dx <= SURFACE_CLEARANCE_RADIUS; dx += 2) {
                for (int dz = -SURFACE_CLEARANCE_RADIUS; dz <= SURFACE_CLEARANCE_RADIUS; dz += 2) {
                    BlockPos column = pos.offset(dx, 0, dz);
                    if (!level().hasChunkAt(column) || !level().getFluidState(column.below()).is(FluidTags.WATER)
                            || !level().getFluidState(column.below(8)).is(FluidTags.WATER)) return null;
                    for (int dy = 0; dy <= BREACH_CLEARANCE_HEIGHT; dy++) {
                        if (!level().getBlockState(column.above(dy)).isAir()) return null;
                    }
                }
            }
            BlockPos water = pos.below();
            double surfaceY = water.getY() + level().getFluidState(water).getHeight(level(), water);
            return new Vec3(getX(), surfaceY, getZ());
        }
        return null;
    }

    @Override
    public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(8.0, 18.0, 8.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 0, state -> {
            return state.setAndContinue(switch (entityData.get(ACTIVITY)) {
                case BREACHING -> BREACH;
                case FEEDING -> FEED;
                default -> state.isMoving() ? SWIM : IDLE;
            });
        }));
    }

    private class SurfaceActivityGoal extends Goal {
        @Nullable private Vec3 surface;
        private int approachTicks;
        private boolean started;

        SurfaceActivityGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (isPerformingSurfaceActivity() || activityCooldown > 0 || !isInWater() || getAirSupply() < 2400
                    || tickCount < nextSurfaceSearchTick) return false;
            nextSurfaceSearchTick = tickCount + 40;
            surface = findSurface();
            return surface != null;
        }

        @Override
        public void start() {
            approachTicks = 0;
            started = false;
        }

        @Override
        public boolean canContinueToUse() {
            return isAlive() && isInWater() && getAirSupply() >= 200
                    && (started ? isPerformingSurfaceActivity() : approachTicks < 2400);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (surface == null || started && !isPerformingSurfaceActivity()) return;
            if (isPerformingSurfaceActivity()) {
                setXRot(0.0F);
                getNavigation().stop();
                return;
            }
            approachTicks++;
            double targetY = surface.y - SURFACE_OFFSET;
            swimTo(new Vec3(surface.x, Math.min(targetY, getY() + 12), surface.z), 1.0);
            if (Math.abs(getY() - targetY) <= 0.5
                    && new Vec3(getX(), 0, getZ()).distanceToSqr(new Vec3(surface.x, 0, surface.z)) <= 2.0) {
                started = tryStartSurfaceActivity(nextActivityIsBreach);
                if (started) nextActivityIsBreach = !nextActivityIsBreach;
                else approachTicks = 2400;
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
            if (isPerformingSurfaceActivity()) stopSurfaceActivity();
            else activityCooldown = Math.max(activityCooldown, 200);
        }
    }
}
