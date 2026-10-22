package net.ussm.examplemod.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.worldgen.TestBiomeSource;

import java.util.function.Supplier;

public class ModBiomeSources {
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, ExampleMod.MOD_ID);

    public static final Supplier<MapCodec<TestBiomeSource>> LOBBY_BIOME_SOURCE =
            BIOME_SOURCES.register("test_biome_source", () -> TestBiomeSource.CODEC);
}
