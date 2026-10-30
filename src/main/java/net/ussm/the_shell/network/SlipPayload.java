package net.ussm.the_shell.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;

// le joueur vient de glisser sur une peau de banane (allonge = true) ou se releve avec espace (false)
public record SlipPayload(boolean allonge) implements CustomPacketPayload {
    public static final Type<SlipPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "slip"));
    public static final StreamCodec<ByteBuf, SlipPayload> STREAM_CODEC = ByteBufCodecs.BOOL.map(SlipPayload::new, SlipPayload::allonge);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
