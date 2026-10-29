package net.ussm.the_shell.init;

import net.ussm.the_shell.TheShell;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

import java.util.function.Supplier;

public class ModChunkGenerators {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, TheShell.MOD_ID);

    public static final Supplier<MapCodec<TestChunkGenerator>> LOBBY_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("test_chunk_generator", () -> TestChunkGenerator.CODEC);
}