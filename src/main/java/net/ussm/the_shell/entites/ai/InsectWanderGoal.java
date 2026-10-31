package net.ussm.the_shell.entites.ai;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;

public class InsectWanderGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private final int radius;
    private final float minHeight;
    private final float maxHeight;
    private final int chance;

    public InsectWanderGoal(PathfinderMob mob, double speed, int radius, float minHeight, float maxHeight, int chance) {
        this.mob = mob;
        this.speed = speed;
        this.radius = radius;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.chance = chance;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return mob.getNavigation().isDone() && mob.getRandom().nextInt(chance) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        RandomSource random = mob.getRandom();
        for (int i = 0; i < 6; i++) {
            int x = mob.getBlockX() + random.nextInt(radius * 2 + 1) - radius;
            int z = mob.getBlockZ() + random.nextInt(radius * 2 + 1) - radius;
            int ground = mob.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            double y = ground + minHeight + random.nextFloat() * (maxHeight - minHeight);
            if (Math.abs(y - mob.getY()) > radius) continue;
            if (!mob.level().getBlockState(BlockPos.containing(x + 0.5, y, z + 0.5)).isAir()) continue;
            if (mob.getNavigation().moveTo(x + 0.5, y, z + 0.5, speed)) return;
        }
    }
}
