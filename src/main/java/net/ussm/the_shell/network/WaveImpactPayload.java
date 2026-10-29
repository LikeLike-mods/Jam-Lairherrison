package net.ussm.the_shell.network;

import net.ussm.the_shell.world.water.ImpactWave;
import java.util.Objects;
import net.ussm.the_shell.TheShell;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WaveImpactPayload(ResourceLocation dimension, ImpactWave wave) implements CustomPacketPayload {
    public static final Type<WaveImpactPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "water_impact"));
    public static final StreamCodec<FriendlyByteBuf, WaveImpactPayload> STREAM_CODEC =
            StreamCodec.of(WaveImpactPayload::encode, WaveImpactPayload::decode);

    public WaveImpactPayload {
        Objects.requireNonNull(dimension);
        Objects.requireNonNull(wave);
    }

    private static void encode(FriendlyByteBuf buf, WaveImpactPayload payload) {
        buf.writeResourceLocation(payload.dimension);
        ImpactWave w = payload.wave;
        buf.writeDouble(w.x());
        buf.writeDouble(w.y());
        buf.writeDouble(w.z());
        buf.writeVarLong(w.startTick());
        buf.writeFloat(w.strength());
        buf.writeFloat(w.initialRadius());
    }

    private static WaveImpactPayload decode(FriendlyByteBuf buf) {
        ResourceLocation dimension = buf.readResourceLocation();
        return new WaveImpactPayload(dimension, new ImpactWave(buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readVarLong(), buf.readFloat(), buf.readFloat()));
    }

    @Override
    public Type<WaveImpactPayload> type() { return TYPE; }
}
