package net.ussm.the_shell.entites.init;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

public final class SpawnRules {
    public static final TagKey<Biome> ABYSS = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "abyss"));

    private SpawnRules() {
    }

    public static <T extends Entity> boolean onSandOrGrass(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return (below.is(BlockTags.SAND) || below.is(BlockTags.ANIMALS_SPAWNABLE_ON)) && level.getRawBrightness(pos, 0) > 8;
    }

    public static <T extends Entity> boolean onSand(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.SAND) && level.getRawBrightness(pos, 0) > 8;
    }

    public static <T extends Entity> boolean onGrass(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    public static <T extends Entity> boolean inLagoon(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        int sea = level.getSeaLevel();
        return level.getFluidState(pos).is(FluidTags.WATER) && pos.getY() < sea && pos.getY() > sea - 20;
    }

    public static boolean inAbyss(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(ABYSS);
    }

    public static <T extends Entity> boolean inDeepWater(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getLevel().getServer().isSpawningMonsters()
                && level.getFluidState(pos).is(FluidTags.WATER)
                && pos.getY() < level.getSeaLevel() - 30
                && level.getRawBrightness(pos, 0) <= 3;
    }

    private static boolean noneNearby(ServerLevelAccessor level, EntityType<?> type, BlockPos pos, double radius) {
        return level.getEntities(type, new AABB(pos).inflate(radius), e -> true).isEmpty();
    }

    public static <T extends Entity> boolean leviathan(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.above(3)).is(FluidTags.WATER)
                && inAbyss(level, pos)
                && noneNearby(level, type, pos, 160);
    }

    public static <T extends Entity> boolean blueWhale(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.above(4)).is(FluidTags.WATER)
                && noneNearby(level, type, pos, 96);
    }

    public static <T extends Entity> boolean basilosaurus(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getFluidState(pos).is(FluidTags.WATER)
                && pos.getY() >= TestChunkGenerator.SEA - 12
                && noneNearby(level, type, pos, 64);
    }

    public static <T extends Entity> boolean purussaurus(EntityType<T> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.below()).is(FluidTags.WATER)
                && noneNearby(level, type, pos, 48);
    }
}
