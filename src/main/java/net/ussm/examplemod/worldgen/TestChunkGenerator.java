package net.ussm.examplemod.worldgen;

import net.ussm.examplemod.api.FastNoiseLite;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.ussm.examplemod.init.ModBlocks;

import java.util.List;
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

    }

    @Override
    public int getGenDepth() {
        return 0;
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

    public static float getHeight(float posX, float posZ) {
        net.ussm.examplemod.api.FastNoiseLite noise = new FastNoiseLite(200);
        float h;

        noise.SetFrequency(0.0001f);
        noise.SetFractalOctaves(1);
        noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        float lines = noise.GetNoise(posX, posZ);
        lines += 1f; lines *= 0.5f;
        float lines2 = lines;
        if (lines > 0.5f) lines2 = (float)Math.abs(Math.cos(3f * Math.PI * lines2) / 4f);
        if (lines > 10f/12f) lines2 *= 2.5f;
        if (lines > 0.5f) lines2 += 0.5f;

        noise.SetSeed(201);
        noise.SetFrequency(0.001f);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(5);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        float islands = noise.GetNoise(posX, posZ);
        islands += 1f; islands *= lines2; islands -= 1f;

        h = islands * 50f;
        return h;
    }

    private void placeMyStructure(ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = -128;

                int posX = x + (chunk.getPos().x * 16);
                int posZ = z + (chunk.getPos().z * 16);

                float h = getHeight(posX, posZ);
                float n = getHeight(posX + 1, posZ);
                float s = getHeight(posX - 1, posZ);
                float e = getHeight(posX, posZ + 1);
                float w = getHeight(posX, posZ - 1);
                float slope = length(n - s, e - w);
                slope *= 2f;
                if (slope > 1f) slope = 1f;

                // Layers
                int a = Math.round(h);
                float b = h - (int)Math.floor(h - 0.5f) - 0.5f;
                int stratum = (int)Math.floor(b * 8);
                stratum = Math.clamp(stratum, 0, 8);


                // Blocks hashes
                net.ussm.examplemod.api.FastNoiseLite noise = new FastNoiseLite(200);
                noise.SetSeed(202);
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

                if (inRange(h, -1f, 5f * slope)) {
                    if (inRange(h, 0f, 5f * slope)) topLand = ModBlocks.LIGHT_SAND.get().defaultBlockState();
                    else topLand = ModBlocks.LIGHT_SAND_WET.get().defaultBlockState();
                    top2Land = ModBlocks.LIGHT_SAND.get().defaultBlockState();
                }

                BlockState topWater = ModBlocks.LIGHT_SAND_WET.get().defaultBlockState();

                BlockState rock = ModBlocks.BROWNSTONE.get().defaultBlockState();
                BlockState bottom = ModBlocks.GREY_GRANITE.get().defaultBlockState();


                //Building
                while (y <= a || y <= 0) {
                    pos.set(x, y, z);

                    if (y == -128) chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
                    else if (y == a) {
                        if (y < 0) {
                            chunk.setBlockState(pos, topWater, false);
                        }
                        else {
                            chunk.setBlockState(pos, topLand, false);
                        }
                    }
                    else if (y == a - 1) {
                        if (y < 0) {
                            chunk.setBlockState(pos, topWater, false);
                        }
                        else {
                            chunk.setBlockState(pos, top2Land, false);
                        }
                    }
                    else if (y < a - 1){
                        chunk.setBlockState(pos, bottom, false);
                    }
                    else if (y <= 0){
                        chunk.setBlockState(pos, Blocks.WATER.defaultBlockState(), false);
                    }

                    y += 1;
                }
            }
        }

    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
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