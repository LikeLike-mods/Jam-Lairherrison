package net.ussm.the_shell.worldgen;

import net.minecraft.util.Mth;
import net.ussm.the_shell.api.FastNoiseLite;
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
import net.ussm.the_shell.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TestChunkGenerator extends ChunkGenerator {

    public static final MapCodec<TestChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
    ).apply(instance, TestChunkGenerator::new));

    public static final Codec<TestChunkGenerator> FULL_CODEC = CODEC.codec();

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
        Set<Holder<Biome>> biomes = new LinkedHashSet<>();
        for (int dx = 2; dx < 16; dx += 4) {
            for (int dz = 2; dz < 16; dz += 4) {
                int x = chunkPos.getMinBlockX() + dx;
                int z = chunkPos.getMinBlockZ() + dz;
                biomes.add(worldGenRegion.getBiome(new BlockPos(x, worldGenRegion.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z)));
            }
        }
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        random.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        for (Holder<Biome> biome : biomes) {
            NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, random);
        }
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

    public static final int SEA = 100;
    public static final int ABYSS = SEA - 64;
    public static final int SEED = 200;

    // Rifts
    public static final int RIFT_DOWN = -118;
    private static final FastNoiseLite RIFT_LINES = riftNoise(SEED + 10, 0.004f, FastNoiseLite.FractalType.Ridged);
    private static final FastNoiseLite RIFT_PIECES = riftNoise(SEED + 11, 0.004f, FastNoiseLite.FractalType.FBm);

    private static FastNoiseLite riftNoise(int seed, float freq, FastNoiseLite.FractalType type) {
        FastNoiseLite n = new FastNoiseLite(seed);
        n.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        n.SetFrequency(freq);
        n.SetFractalType(type);
        n.SetFractalOctaves(1);
        return n;
    }

    public static boolean isRift(int x, int z) {
        if ((RIFT_LINES.GetNoise(x, z) + 1f) * 0.5f < 0.92f) return false;
        if (RIFT_PIECES.GetNoise(x, z) < -0.25f) return false;
        return getHeight(x, z) < ABYSS - 80;
    }

    public static float getHeight(float posX, float posZ) {
        net.ussm.the_shell.api.FastNoiseLite noise = new FastNoiseLite(SEED);
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

        noise.SetSeed(SEED + 1);
        noise.SetFrequency(0.001f);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(5);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        float islands = noise.GetNoise(posX, posZ);
        islands += 1f; islands *= lines2; islands -= 1f;

        noise.SetSeed(SEED + 2);
        noise.SetFrequency(0.01f);
        noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
        noise.SetFractalOctaves(1);
        float trenches = noise.GetNoise(posX, posZ);
        trenches += 1f; trenches *= (float)Mth.smoothstep(Math.clamp(-islands, 0f, 1f)); trenches /= 2f;
        islands = Math.clamp(islands - 3f * trenches, islands - 1.2f, 1);

        float mountains = Math.clamp(1.5f * islands - 1f, 0f, 1f);
        islands += 3f * mountains;

        float depth = -islands * 50f;
        if (depth > 40f) depth += (depth - 40f) * 1.2f;
        h = Math.max(-120f, SEA - depth);
        return h;
    }

    // Swamp height
    public static final int SWAMP_LEVEL = SEA + 2;

    // To swamp or not to swamp, that is the question
    public static float getSwampiness(float posX, float posZ, float posY) {
        net.ussm.the_shell.api.FastNoiseLite noise = new FastNoiseLite(SEED + 3);
        noise.SetFrequency(0.001f);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(1);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);

        float swamp = Math.clamp(4f * noise.GetNoise(posX, posZ) - 1.4f, 0f, 1f);
        float bottom = Math.clamp(1f - (posY - SWAMP_LEVEL) * 0.125f, 0f, 1f);
        float land = Math.clamp(posY - SEA, 0f, 1f); // On land only
        return (float)(Mth.smoothstep(swamp) * Mth.smoothstep(bottom)) * land;
    }

    public static float lerp(float a, float b, float t) {
        return a - (b - a) * t;
    }

    private void placeMyStructure(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int cx = chunk.getPos().x * 16;
        int cz = chunk.getPos().z * 16;

        float[][] ground = new float[18][18];
        float[][] swamp = new float[18][18];
        for (int i = 0; i < 18; i++) {
            for (int j = 0; j < 18; j++) {
                float raw = getHeight(cx + i - 1, cz + j - 1);
                swamp[i][j] = getSwampiness(cx + i - 1, cz + j - 1, raw);
                ground[i][j] = raw + (SWAMP_LEVEL - raw) * swamp[i][j];
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = -128;

                int posX = x + (chunk.getPos().x * 16);
                int posZ = z + (chunk.getPos().z * 16);

                float h = ground[x + 1][z + 1];
                float n = ground[x + 2][z + 1];
                float s = ground[x][z + 1];
                float e = ground[x + 1][z + 2];
                float w = ground[x + 1][z];
                float slope = length(n - s, e - w);
                slope *= 2f;

                // Block hashes
                net.ussm.the_shell.api.FastNoiseLite noise = new FastNoiseLite(202);
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

                float swampiness = swamp[x + 1][z + 1];

                if (slope >= 7f) {
                    topLand = ModBlocks.BROWNSTONE.get().defaultBlockState();
                    top2Land = ModBlocks.BROWNSTONE.get().defaultBlockState();
                    topWater = ModBlocks.GREY_GRANITE.get().defaultBlockState();
                }
                else if (swampiness >= 0.5f) {
                    boolean pool = Math.round(h) >= SWAMP_LEVEL && Math.round(n) >= Math.round(h) && Math.round(s) >= Math.round(h)
                            && Math.round(e) >= Math.round(h) && Math.round(w) >= Math.round(h);
                    if (pool) topLand = Blocks.WATER.defaultBlockState();
                    else topLand = Blocks.MUD.defaultBlockState();
                    top2Land = Blocks.MUD.defaultBlockState();
                }
                else if (inRange(h, SEA - 1f, SEA + 5f * Math.min(slope, 1f))) {
                    if (inRange(h, SEA, SEA + 5f * Math.min(slope, 1f))) topLand = ModBlocks.LIGHT_SAND.get().defaultBlockState();
                    else topLand = ModBlocks.LIGHT_SAND_WET.get().defaultBlockState();
                    top2Land = ModBlocks.LIGHT_SAND.get().defaultBlockState();
                }
                if (h >= SEA) bottom = ModBlocks.BROWNSTONE.get().defaultBlockState();

                // Layers
                int a = Math.round(h);
                float b = h - (int)Math.floor(h - 0.5f) - 0.5f;
                int stratum = (int)Math.floor(b * 8);
                stratum = Math.clamp(stratum, 0, 8);

                //Building
                while (y <= a || y <= SEA) {
                    pos.set(x, y, z);

                    if (y == -128) chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    else if (y == a) {
                        if (y < SEA) {
                            chunk.setBlockState(pos, topWater, false);
                        }
                        else {
                            chunk.setBlockState(pos, topLand, false);
                        }
                    }
                    else if (y == a - 1) {
                        if (y < SEA) {
                            chunk.setBlockState(pos, topWater, false);
                        }
                        else {
                            chunk.setBlockState(pos, top2Land, false);
                        }
                    }
                    else if (y < a - 1){
                        chunk.setBlockState(pos, bottom, false);
                    }
                    else if (y <= SEA){
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                    }
                    y += 1;
                }

                // Rifts
                if (isRift(posX, posZ)) {
                    chunk.setBlockState(pos.set(x, -128, z), ModBlocks.ABYSSAL_VOID.get().defaultBlockState(), false);
                    for (int y2 = -127; y2 <= a; y2++) {
                        chunk.setBlockState(pos.set(x, y2, z), Blocks.WATER.defaultBlockState(), false);
                    }
                }
            }
        }

    }

    @Override
    public int getSeaLevel() {
        return SEA;
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