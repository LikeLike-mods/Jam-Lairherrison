package net.ussm.examplemod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

public class TestBiomeSource extends BiomeSource {

    public static final MapCodec<TestBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.CODEC.fieldOf("sea")
                            .forGetter(source -> source.sea),
                    Biome.CODEC.fieldOf("shore")
                            .forGetter(source -> source.shore),
                    Biome.CODEC.fieldOf("inland")
                            .forGetter(source -> source.inland),
                    Biome.CODEC.fieldOf("mountain")
                            .forGetter(source -> source.mountain)
            ).apply(instance, TestBiomeSource::new));

    private final Holder<Biome> sea;
    private final Holder<Biome> shore;
    private final Holder<Biome> inland;
    private final Holder<Biome> mountain;

    public TestBiomeSource(Holder<Biome> sea, Holder<Biome> shore, Holder<Biome> inland, Holder<Biome> mountain) {
        this.sea = sea;
        this.shore = shore;
        this.inland = inland;
        this.mountain = mountain;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(sea, shore, mountain);
    }

    private boolean inRange(float t, float a, float b) {
        return (t >= a) && (t <= b);
    }

    @Override
    public Holder<Biome> getNoiseBiome(
            int x, int y, int z,
            Climate.Sampler sampler
    ) {
        int blockX = x * 4;
        int blockZ = z * 4;
        int blockY = y * 4;

        float h = Math.round(TestChunkGenerator.getHeight(blockX, blockZ));

        if (h < 1) return sea;
        if (inRange(h, 1, 5)) return shore;
        return inland;
    }
}