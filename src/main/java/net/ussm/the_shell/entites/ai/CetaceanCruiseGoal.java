package net.ussm.the_shell.entites.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.init.CetaceanEntity;

/** Long, mostly forward swimming legs, with a fresh route when progress stalls. */
public final class CetaceanCruiseGoal extends Goal {
    private final CetaceanEntity mob;
    private final double speed;
    private final int minDistance;
    private final int maxDistance;
    private Path path;
    private Vec3 progressPosition;
    private int ticks;
    private int retryAt;

    public CetaceanCruiseGoal(CetaceanEntity mob, double speed, int minDistance, int maxDistance) {
        this.mob = mob;
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!mob.isInWater() || mob.getTarget() != null || mob.tickCount < retryAt) return false;
        retryAt = mob.tickCount + 20;
        for (int attempt = 0; attempt < 12; attempt++) {
            double yaw = Math.toRadians(mob.getYRot() + (mob.getRandom().nextDouble() - 0.5) * (attempt < 8 ? 120 : 360));
            double distance = attempt < 8
                    ? minDistance + mob.getRandom().nextDouble() * (maxDistance - minDistance)
                    : minDistance * (0.6 + mob.getRandom().nextDouble() * 0.4);
            Vec3 point = mob.position().add(-Math.sin(yaw) * distance, mob.getRandom().nextInt(7) - 3, Math.cos(yaw) * distance);
            BlockPos block = BlockPos.containing(point);
            if (!mob.level().hasChunkAt(block) || !mob.level().getFluidState(block).is(FluidTags.WATER)) continue;
            if (!mob.level().noCollision(mob, mob.getBoundingBox().move(point.subtract(mob.position())))) continue;
            Path candidate = mob.getNavigation().createPath(block, 0);
            if (candidate != null && candidate.canReach()) {
                path = candidate;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        ticks = 0;
        progressPosition = mob.position();
        mob.getNavigation().moveTo(path, speed);
    }

    @Override
    public boolean canContinueToUse() {
        return mob.getTarget() == null && mob.isInWater() && !mob.getNavigation().isDone() && ticks < 600;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (++ticks % 80 == 0) {
            if (mob.position().distanceToSqr(progressPosition) < 1.0) mob.getNavigation().stop();
            progressPosition = mob.position();
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}
