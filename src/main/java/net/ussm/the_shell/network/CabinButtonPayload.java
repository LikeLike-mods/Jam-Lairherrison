package net.ussm.the_shell.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;

public record CabinButtonPayload() implements CustomPacketPayload {
    public static final CabinButtonPayload INSTANCE = new CabinButtonPayload();
    public static final Type<CabinButtonPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "cabin_button"));
    public static final StreamCodec<ByteBuf, CabinButtonPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
