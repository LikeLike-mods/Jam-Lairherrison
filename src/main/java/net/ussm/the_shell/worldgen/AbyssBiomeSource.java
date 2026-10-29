package net.ussm.the_shell.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

public class AbyssBiomeSource extends BiomeSource {
    public static final MapCodec<AbyssBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.fieldOf("plains").forGetter(s -> s.plains),
            Biome.CODEC.fieldOf("vents").forGetter(s -> s.vents),
            Biome.CODEC.fieldOf("trench").forGetter(s -> s.trench),
            Biome.CODEC.fieldOf("reef").forGetter(s -> s.reef)
    ).apply(instance, AbyssBiomeSource::new));

    private final Holder<Biome> plains;
    private final Holder<Biome> vents;
    private final Holder<Biome> trench;
    private final Holder<Biome> reef;

    public AbyssBiomeSource(Holder<Biome> plains, Holder<Biome> vents, Holder<Biome> trench, Holder<Biome> reef) {
        this.plains = plains;
        this.vents = vents;
        this.trench = trench;
        this.reef = reef;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(plains, vents, trench, reef);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return switch (AbyssTerrain.zone(x * 4 + 2, z * 4 + 2)) {
            case VENTS -> vents;
            case TRENCH -> trench;
            case REEF -> reef;
            default -> plains;
        };
    }
}
