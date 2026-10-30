package net.ussm.the_shell.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;

public record SlipPayload(boolean lyingDown) implements CustomPacketPayload {
    public static final Type<SlipPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "slip"));
    public static final StreamCodec<ByteBuf, SlipPayload> STREAM_CODEC = ByteBufCodecs.BOOL.map(SlipPayload::new, SlipPayload::lyingDown);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
