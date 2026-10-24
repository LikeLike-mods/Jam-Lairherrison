package net.ussm.examplemod.entity.init;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public final class SpawnRules {
    private SpawnRules() {
    }

    public static <T extends Entity> boolean onSandOrGrass(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return (below.is(BlockTags.SAND) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)) && level.getRawBrightness(pos, 0) > 8;
    }

    public static <T extends Entity> boolean inLagoon(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        int sea = level.getSeaLevel();
        return level.getFluidState(pos).is(FluidTags.WATER) && pos.getY() < sea && pos.getY() > sea - 20;
    }

    public static <T extends Entity> boolean inDeepWater(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getFluidState(pos).is(FluidTags.WATER)
                && pos.getY() < level.getSeaLevel() - 30
                && level.getRawBrightness(pos, 0) <= 3;
    }
}
