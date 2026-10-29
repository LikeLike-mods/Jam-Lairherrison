package net.ussm.the_shell.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.ussm.the_shell.init.ModBlocks;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// la dimension des abysses : de l'eau partout entre un fond et un plafond de roche,
// avec en haut les failles qui ramenent dans l'atoll (aux memes x z que les failles de l'atoll)
public class AbyssChunkGenerator extends ChunkGenerator {
    public static final MapCodec<AbyssChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.biomeSource)
    ).apply(instance, AbyssChunkGenerator::new));

    public AbyssChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // ce qui recouvre le fond selon la zone
    private static BlockState surface(AbyssTerrain.Zone zone) {
        return switch (zone) {
            case VENTS -> ModBlocks.ABYSSAL_BASALT.get().defaultBlockState();
            case TRENCH -> ModBlocks.ABYSSAL_STONE.get().defaultBlockState();
            default -> ModBlocks.ABYSSAL_SILT.get().defaultBlockState();
        };
    }

    // un bloc de la colonne x z a la hauteur y (sert aussi pour getBaseColumn)
    private static BlockState at(int x, int y, int z, int floor, int ceiling, boolean rift, AbyssTerrain.Zone zone) {
        if (y <= AbyssTerrain.MIN_Y || y >= AbyssTerrain.TOP) return Blocks.BEDROCK.defaultBlockState();
        if (y <= floor) {
            if (y > floor - 3) return surface(zone);
            if (y < AbyssTerrain.MIN_Y + 3 && ((x * 31 + y * 17 + z * 13) & 3) == 0) return Blocks.BEDROCK.defaultBlockState();
            return ModBlocks.ABYSSAL_STONE.get().defaultBlockState();
        }
        if (y < ceiling) return Blocks.WATER.defaultBlockState();
        // le puits de la faille remonte dans le plafond jusqu'en haut : en y nageant on retourne dans l'atoll
        if (rift) return Blocks.WATER.defaultBlockState();
        return ModBlocks.ABYSSAL_STONE.get().defaultBlockState();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap floorMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surfaceMap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunk.getPos().getMinBlockX() + dx;
                int z = chunk.getPos().getMinBlockZ() + dz;
                int floor = AbyssTerrain.floor(x, z);
                int ceiling = AbyssTerrain.ceiling(x, z);
                boolean rift = TestChunkGenerator.isRift(x, z);
                AbyssTerrain.Zone zone = AbyssTerrain.zone(x, z);
                for (int y = AbyssTerrain.MIN_Y; y <= AbyssTerrain.TOP; y++) {
                    BlockState state = at(x, y, z, floor, ceiling, rift, zone);
                    chunk.setBlockState(pos.set(dx, y, dz), state, false);
                    floorMap.update(dx, y, dz, state);
                    surfaceMap.update(dx, y, dz, state);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return switch (type) {
            case OCEAN_FLOOR, OCEAN_FLOOR_WG -> AbyssTerrain.floor(x, z) + 1;
            default -> AbyssTerrain.TOP + 1;
        };
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        int floor = AbyssTerrain.floor(x, z);
        int ceiling = AbyssTerrain.ceiling(x, z);
        boolean rift = TestChunkGenerator.isRift(x, z);
        AbyssTerrain.Zone zone = AbyssTerrain.zone(x, z);
        BlockState[] states = new BlockState[AbyssTerrain.HEIGHT];
        for (int i = 0; i < states.length; i++) states[i] = at(x, AbyssTerrain.MIN_Y + i, z, floor, ceiling, rift, zone);
        return new NoiseColumn(AbyssTerrain.MIN_Y, states);
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomes, StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {
    }

    // comme dans l'atoll : les mobs des biomes apparaissent des la creation du chunk
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        ChunkPos chunkPos = region.getCenter();
        Set<Holder<Biome>> biomes = new LinkedHashSet<>();
        for (int dx = 2; dx < 16; dx += 4) {
            for (int dz = 2; dz < 16; dz += 4) {
                int x = chunkPos.getMinBlockX() + dx;
                int z = chunkPos.getMinBlockZ() + dz;
                biomes.add(region.getBiome(new BlockPos(x, AbyssTerrain.floor(x, z) + 2, z)));
            }
        }
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        random.setDecorationSeed(region.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        for (Holder<Biome> biome : biomes) NaturalSpawner.spawnMobsForChunkGeneration(region, biome, chunkPos, random);
    }

    @Override
    public int getGenDepth() {
        return AbyssTerrain.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return AbyssTerrain.CEILING;
    }

    @Override
    public int getMinY() {
        return AbyssTerrain.MIN_Y;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
    }
}
