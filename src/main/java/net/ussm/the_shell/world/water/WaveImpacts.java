package net.ussm.the_shell.world.water;

import net.ussm.the_shell.network.WaveImpactPayload;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WaveImpacts {
    public static final ResourceLocation DIMENSION = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "test_dim");
    private static final double RECEIVE_DISTANCE_SQUARED = 384.0 * 384.0;

    private WaveImpacts() {}

    public static void whaleSplash(ServerLevel level, Vec3 waterSurface) {
        if (!level.dimension().location().equals(DIMENSION)) return;
        if (!level.getServer().isSameThread()) {
            throw new IllegalStateException("Emit water impacts on the server game thread");
        }
        ImpactWave wave = new ImpactWave(waterSurface.x, waterSurface.y, waterSurface.z,
                level.getGameTime(), ImpactWave.DEFAULT_STRENGTH, ImpactWave.DEFAULT_RADIUS);
        WaveImpactPayload payload = new WaveImpactPayload(DIMENSION, wave);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(wave.x(), wave.y(), wave.z()) <= RECEIVE_DISTANCE_SQUARED
                    && player.connection.hasChannel(WaveImpactPayload.TYPE)) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
