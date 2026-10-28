package net.ussm.examplemod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

import static net.ussm.examplemod.worldgen.TestChunkGenerator.*;

public class TestBiomeSource extends BiomeSource {

    public static final MapCodec<TestBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.CODEC.fieldOf("abyss")
                            .forGetter(source -> source.abyss),
                    Biome.CODEC.fieldOf("sea")
                            .forGetter(source -> source.sea),
                    Biome.CODEC.fieldOf("shore")
                            .forGetter(source -> source.shore),
                    Biome.CODEC.fieldOf("inland")
                            .forGetter(source -> source.inland),
                    Biome.CODEC.fieldOf("swamp")
                            .forGetter(source -> source.swamp)
            ).apply(instance, TestBiomeSource::new));

    private final Holder<Biome> abyss;
    private final Holder<Biome> sea;
    private final Holder<Biome> shore;
    private final Holder<Biome> inland;
    private final Holder<Biome> swamp;

    public TestBiomeSource(Holder<Biome> abyss, Holder<Biome> sea, Holder<Biome> shore, Holder<Biome> inland, Holder<Biome> swamp) {
        this.abyss = abyss;
        this.sea = sea;
        this.shore = shore;
        this.inland = inland;
        this.swamp = swamp;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(abyss, sea, shore, inland, swamp);
    }

    private boolean inRange(float t, float a, float b) {
        return (t >= a) && (t <= b);
    }

    private static float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public Holder<Biome> getNoiseBiome(
            int x, int y, int z,
            Climate.Sampler sampler
    ) {
        int blockX = x * 4;
        int blockZ = z * 4;
        int blockY = y * 4;

        float h = Math.round(getHeight(blockX, blockZ));
        float n = getHeight(blockX + 1, blockZ);
        float s = getHeight(blockX - 1, blockZ);
        float e = getHeight(blockX, blockZ + 1);
        float w = getHeight(blockX, blockZ - 1);
        float slope = length(n - s, e - w);
        slope *= 2f;
        if (slope > 1f) slope = 1f;

        float swampiness = getSwampiness(blockX, blockZ, h, Math.min(slope, 1f));

        if (h < SEA + 1f) {
            if (blockY < ABYSS) return abyss;
            else return sea;
        }
        if (swampiness >= 0.5f) return swamp;
        if (inRange(h, SEA - 1f, SEA + 5f * slope)) return shore;
        return inland;
    }
}