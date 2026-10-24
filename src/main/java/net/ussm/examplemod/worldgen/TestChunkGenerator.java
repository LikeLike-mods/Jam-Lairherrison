package net.ussm.examplemod.worldgen;

import net.minecraft.util.Mth;
import net.ussm.examplemod.api.FastNoiseLite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.ussm.examplemod.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestChunkGenerator extends ChunkGenerator {

    public static final MapCodec<TestChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
    ).apply(instance, TestChunkGenerator::new));

    public static final Codec<TestChunkGenerator> FULL_CODEC = CODEC.codec();

    private static int seed;

    public static void setSeed(long worldSeed) {
        seed = Long.hashCode(worldSeed);
    }

    public TestChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        ChunkPos chunkPos = worldGenRegion.getCenter();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        Holder<Biome> biome = worldGenRegion.getBiome(new BlockPos(x, worldGenRegion.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z));
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        random.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, random);
    }

    @Override
    public int getGenDepth() {
        return 512;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        placeMyStructure(chunkAccess);
        return CompletableFuture.completedFuture(chunkAccess);
    }

    private static float length(float x, float y) {
        return (float)Math.sqrt(x*x + y*y);
    }

    private boolean inRange(float t, float a, float b) {
        return (t >= a) && (t <= b);
    }

    public static float getHeight(float posX, float posZ, float bonus) {
        net.ussm.examplemod.api.FastNoiseLite noise = new FastNoiseLite(seed + 200);
        float h;

        noise.SetFrequency(0.0001f);
        noise.SetFractalOctaves(1);
        noise.SetFractalGain(0.5f);
        noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        float lines = noise.GetNoise(posX, posZ);
        lines += 1f; lines *= 0.5f;
        float lines2 = lines;
        if (lines > 0.5f) lines2 = (float)Math.abs(Math.cos(3f * Math.PI * lines2) / 4f);
        if (lines > 10f/12f) lines2 *= 2.5f;
        if (lines > 0.5f) lines2 += 0.5f;

        noise.SetSeed(seed + 201);
        noise.SetFrequency(0.001f);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(5);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        float islands = noise.GetNoise(posX, posZ);
        islands += 1f; islands *= lines2; islands -= 1f;

        noise.SetSeed(seed + 201);
        noise.SetFrequency(0.01f);
        noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
        noise.SetFractalOctaves(1);
        float trenches = noise.GetNoise(posX, posZ);
        trenches += 1f; trenches *= (float)Mth.smoothstep(Math.clamp(-islands, 0f, 1f)); trenches /= 2f;
        islands = Math.clamp(islands - 3f * trenches, islands - 1.2f, 1);

        float mountains = Math.clamp(1.5f * islands - 1f, 0f, 1f);
        islands += 3f * mountains;

        h = islands * 50f + 64f;
        return h;
    }

    private void placeMyStructure(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = -128;

                int posX = x + (chunk.getPos().x * 16);
                int posZ = z + (chunk.getPos().z * 16);

                float h = getHeight(posX, posZ, 0f);
                float n = getHeight(posX + 1, posZ, 0f);
                float s = getHeight(posX - 1, posZ, 0f);
                float e = getHeight(posX, posZ + 1, 0f);
                float w = getHeight(posX, posZ - 1, 0f);
                float slope = length(n - s, e - w);
                slope *= 2f;

                // Layers
                int a = Math.round(h);
                float b = h - (int)Math.floor(h - 0.5f) - 0.5f;
                int stratum = (int)Math.floor(b * 8);
                stratum = Math.clamp(stratum, 0, 8);

                // Blocks hashes
                net.ussm.examplemod.api.FastNoiseLite noise = new FastNoiseLite(seed + 202);
                noise.SetFrequency(0.1f);
                noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
                float hash1 = noise.GetNoise(x + (chunk.getPos().x * 16), z + (chunk.getPos().z * 16));

                noise.SetFrequency(0.01f);
                noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
                float hash2 = noise.GetNoise(x + (chunk.getPos().x * 16), z + (chunk.getPos().z * 16));
                hash2 += 1;

                noise.SetFrequency(1f);
                noise.SetNoiseType(FastNoiseLite.NoiseType.Value);
                float hash3 = noise.GetNoise(x + (chunk.getPos().x * 16), z + (chunk.getPos().z * 16));

                // Blocks used
                BlockState topLand = Blocks.GRASS_BLOCK.defaultBlockState();
                BlockState top2Land = Blocks.DIRT.defaultBlockState();
                BlockState topWater = ModBlocks.LIGHT_SAND_WET.get().defaultBlockState();
                BlockState bottom = ModBlocks.GREY_GRANITE.get().defaultBlockState();

                if (inRange(h, 63f, 64f + 5f * Math.min(slope, 1f))) {
                    if (inRange(h, 64f, 64f + 5f * Math.min(slope, 1f))) topLand = ModBlocks.LIGHT_SAND.get().defaultBlockState();
                    else topLand = ModBlocks.LIGHT_SAND_WET.get().defaultBlockState();
                    top2Land = ModBlocks.LIGHT_SAND.get().defaultBlockState();
                }
                else if (slope >= 7f) {
                    topLand = ModBlocks.BROWNSTONE.get().defaultBlockState();
                    top2Land = ModBlocks.BROWNSTONE.get().defaultBlockState();
                    topWater = ModBlocks.GREY_GRANITE.get().defaultBlockState();
                }
                if (h >= 0f) bottom = ModBlocks.BROWNSTONE.get().defaultBlockState();

                //Building
                while (y <= a || y <= 64) {
                    pos.set(x, y, z);

                    if (y == -128) chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    else if (y == a) {
                        if (y < 64) {
                            chunk.setBlockState(pos, topWater, false);
                        }
                        else {
                            chunk.setBlockState(pos, topLand, false);
                        }
                    }
                    else if (y == a - 1) {
                        if (y < 64) {
                            chunk.setBlockState(pos, topWater, false);
                        }
                        else {
                            chunk.setBlockState(pos, top2Land, false);
                        }
                    }
                    else if (y < a - 1){
                        chunk.setBlockState(pos, bottom, false);
                    }
                    else if (y <= 64){
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                    }
                    y += 1;
                }
            }
        }

    }

    @Override
    public int getSeaLevel() {
        return 64;
    }

    @Override
    public int getMinY() {
        return -128;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor world, RandomState random) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState random) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState random, BlockPos pos) {}


    @Override
    public void createStructures(RegistryAccess registryAccess, ChunkGeneratorStructureState structureState, StructureManager structureManager, ChunkAccess chunk, StructureTemplateManager structureTemplateManager) {
    }


}