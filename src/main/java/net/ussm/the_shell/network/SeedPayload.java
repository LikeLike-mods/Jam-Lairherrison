package net.ussm.the_shell.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;

public record SeedPayload(int seed) implements CustomPacketPayload {
    public static final Type<SeedPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "seed"));
    public static final StreamCodec<ByteBuf, SeedPayload> STREAM_CODEC = ByteBufCodecs.INT.map(SeedPayload::new, SeedPayload::seed);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
