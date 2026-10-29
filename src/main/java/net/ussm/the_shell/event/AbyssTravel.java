package net.ussm.the_shell.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.worldgen.AbyssTerrain;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

// passer par une faille : de l'atoll on tombe sous le plafond des abysses, et en remontant dans la faille du plafond on revient
// dans la faille de l'atoll. les deux failles sont aux memes x z, donc on ressort toujours la ou on etait entre
public class AbyssTravel {
    public static final ResourceKey<Level> ABYSS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "abyss"));
    private static final int COOLDOWN = 100;

    public static void travel(Entity entity, ServerLevel from) {
        // les passagers voyagent avec leur vehicule (le sous-marin), pas tout seuls
        if (entity.isPassenger() || entity.isOnPortalCooldown() || !entity.canUsePortal(true)) return;
        boolean down = from.dimension() != ABYSS;
        ServerLevel to = from.getServer().getLevel(down ? ABYSS : AtollTravel.ATOLL);
        if (to == null) return;

        int x = entity.getBlockX(), z = entity.getBlockZ();
        double y = down ? AbyssTerrain.ceiling(x, z) - 4 : TestChunkGenerator.RIFT_TOP + 14;
        entity.setPortalCooldown(COOLDOWN);
        entity.getPassengers().forEach(p -> p.setPortalCooldown(COOLDOWN));
        from.playSound(null, entity.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.AMBIENT, 0.25F, down ? 0.6F : 0.9F);
        entity.changeDimension(new DimensionTransition(to, new Vec3(x + 0.5, y, z + 0.5), Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                DimensionTransition.DO_NOTHING));
    }
}
