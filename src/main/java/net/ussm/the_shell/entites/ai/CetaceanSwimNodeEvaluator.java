package net.ussm.the_shell.entites.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.level.pathfinder.Target;

/** Water paths that also admit a large mammal's partly exposed breathing posture. */
public final class CetaceanSwimNodeEvaluator extends SwimNodeEvaluator {
    public CetaceanSwimNodeEvaluator() {
        super(true);
    }

    @Override
    public Target getTarget(double x, double y, double z) {
        return getTargetNodeAt(x - entityWidth * 0.5, y, z - entityDepth * 0.5);
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        boolean water = false;
        boolean air = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < entityWidth; dx++) {
            for (int dy = 0; dy < entityHeight; dy++) {
                for (int dz = 0; dz < entityDepth; dz++) {
                    pos.set(x + dx, y + dy, z + dz);
                    BlockState state = context.getBlockState(pos);
                    if (!state.getCollisionShape(context.level(), pos).isEmpty()) return PathType.BLOCKED;
                    if (state.getFluidState().is(FluidTags.WATER)) water = true;
                    else if (state.isAir()) air = true;
                    else return PathType.BLOCKED;
                }
            }
        }
        if (!water) return PathType.BLOCKED;
        return air ? PathType.BREACH : PathType.WATER;
    }
}
