package net.ussm.the_shell.events;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.worldgen.AbyssTerrain;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class AbyssTravel {
    public static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "abyss"));
    public static final int UP = AbyssTerrain.TOP - 8;
    private static final int COOLDOWN = 40;

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        check(event.getEntity());
    }

    public static void check(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || entity.isPassenger() || entity.isOnPortalCooldown()) return;
        boolean atoll = level.dimension() == AtollTravel.ATOLL;
        if (atoll ? entity.getY() >= TestChunkGenerator.RIFT_DOWN : level.dimension() != ABYSS || entity.getY() <= UP) return;
        if (!TestChunkGenerator.isRift(entity.getBlockX(), entity.getBlockZ()) || !entity.canUsePortal(true)) return;
        ServerLevel to = level.getServer().getLevel(atoll ? ABYSS : AtollTravel.ATOLL);
        if (to == null) return;
        double y = atoll ? UP - 6 : TestChunkGenerator.RIFT_DOWN + 6;
        entity.setPortalCooldown(COOLDOWN);
        entity.getPassengers().forEach(p -> p.setPortalCooldown(COOLDOWN));
        entity.changeDimension(new DimensionTransition(to, new Vec3(entity.getX(), y, entity.getZ()), entity.getDeltaMovement(),
                entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING));
    }
}
