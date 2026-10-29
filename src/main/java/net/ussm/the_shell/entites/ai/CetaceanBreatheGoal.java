package net.ussm.the_shell.entites.ai;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.init.CetaceanEntity;

/** Start ascending with two minutes of air left, not the dolphin's seven seconds. */
public final class CetaceanBreatheGoal extends Goal {
    private final CetaceanEntity mob;
    @Nullable private Vec3 destination;
    private int ticks;

    public CetaceanBreatheGoal(CetaceanEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return mob.isInWaterOrBubble() && mob.getAirSupply() < mob.getMaxAirSupply() / 2;
    }

    @Override
    public boolean canContinueToUse() {
        return mob.isAlive() && mob.isInWaterOrBubble() && mob.getAirSupply() < mob.getMaxAirSupply() - 20;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        ticks = 0;
        destination = null;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (destination == null || ticks % 100 == 0) destination = findAir();
        ticks++;
        if (destination != null) {
            // Stage long ascents so the target stays within the navigation's search range.
            double nextY = Math.min(destination.y, mob.getY() + 12);
            mob.swimTo(new Vec3(destination.x, nextY, destination.z), 1.2);
        }
    }

    @Nullable
    private Vec3 findAir() {
        for (int radius : new int[]{0, 8, 16, 24}) {
            for (int direction = 0; direction < (radius == 0 ? 1 : 8); direction++) {
                double angle = direction * Math.PI / 4;
                BlockPos.MutableBlockPos pos = BlockPos.containing(mob.getX() + Math.cos(angle) * radius,
                        mob.getY(), mob.getZ() + Math.sin(angle) * radius).mutable();
                if (!mob.level().hasChunkAt(pos)) continue;
                for (int y = pos.getY(); y < mob.level().getMaxBuildHeight() - 1; y++) {
                    pos.setY(y);
                    if (mob.level().getFluidState(pos).is(FluidTags.WATER)) continue;
                    if (mob.level().getBlockState(pos).isAir() && mob.level().getFluidState(pos.below()).is(FluidTags.WATER)) {
                        double waterY = pos.getY() - 1 + mob.level().getFluidState(pos.below()).getHeight(mob.level(), pos.below());
                        double feetY = Math.ceil(waterY - mob.getEyeHeight() + 0.25);
                        return new Vec3(pos.getX() + 0.5, feetY, pos.getZ() + 0.5);
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }
}
