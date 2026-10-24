package net.ussm.examplemod.entity;

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

// Où les mobs de l'atoll peuvent apparaître naturellement (les biomes disent lesquels, ces règles disent où).
public final class AtollSpawnRules {
    private AtollSpawnRules() {
    }

    // Plage et jungle : sur le sable ou l'herbe, en plein jour
    public static <T extends Entity> boolean onSandOrGrass(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return (below.is(BlockTags.SAND) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)) && level.getRawBrightness(pos, 0) > 8;
    }

    // Lagon : dans l'eau, pas trop loin de la surface
    public static <T extends Entity> boolean inLagoon(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        int sea = level.getSeaLevel();
        return level.getFluidState(pos).is(FluidTags.WATER) && pos.getY() < sea && pos.getY() > sea - 20;
    }

    // Profondeurs : dans l'eau, loin sous la surface et dans le noir (là où la lumière du ciel ne descend plus)
    public static <T extends Entity> boolean inDeepWater(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getFluidState(pos).is(FluidTags.WATER)
                && pos.getY() < level.getSeaLevel() - 30
                && level.getRawBrightness(pos, 0) <= 3;
    }
}
