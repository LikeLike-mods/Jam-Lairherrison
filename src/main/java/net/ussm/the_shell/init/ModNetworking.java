package net.ussm.the_shell.init;

import java.util.function.Consumer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.ussm.the_shell.network.WaveImpactPayload;

/** Common payload registration; the client entry point supplies client-only handlers. */
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
    }
}
