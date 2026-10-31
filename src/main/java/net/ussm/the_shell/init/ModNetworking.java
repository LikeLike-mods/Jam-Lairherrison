package net.ussm.the_shell.init;

import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.ussm.the_shell.events.SlipEvents;
import net.ussm.the_shell.events.SubmarineInterior;
import net.ussm.the_shell.network.CabinButtonPayload;
import net.ussm.the_shell.network.SlipPayload;
import net.ussm.the_shell.network.WaveImpactPayload;

public final class ModNetworking {
    private static Consumer<WaveImpactPayload> waveImpactHandler = ignored -> {};

    private ModNetworking() {}

    public static void register(IEventBus bus) {
        bus.addListener(ModNetworking::registerPayloads);
    }

    public static void setWaveImpactHandler(Consumer<WaveImpactPayload> handler) {
        waveImpactHandler = handler;
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(WaveImpactPayload.TYPE, WaveImpactPayload.STREAM_CODEC,
                (payload, context) -> waveImpactHandler.accept(payload));
        event.registrar("1").optional().playToServer(SlipPayload.TYPE, SlipPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) SlipEvents.received(player, payload.lyingDown());
                }));
        event.registrar("1").optional().playToServer(CabinButtonPayload.TYPE, CabinButtonPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) SubmarineInterior.downFromSeat(player);
                }));
    }
}
