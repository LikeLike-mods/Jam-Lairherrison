package net.ussm.the_shell.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.network.SeedPayload;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

// the islands, the rifts and the abyss are generated from the seed of the world
@EventBusSubscriber(modid = TheShell.MOD_ID)
public class WorldSeed {

    @SubscribeEvent
    public static void starting(ServerAboutToStartEvent event) {
        TestChunkGenerator.reseed(event.getServer().getWorldData().worldGenOptions().seed());
    }

    // a client on a dedicated server needs the same seed for the sonar and the waves
    @SubscribeEvent
    public static void joined(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player, new SeedPayload(TestChunkGenerator.SEED));
    }
}
