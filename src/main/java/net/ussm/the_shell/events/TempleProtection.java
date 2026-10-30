package net.ussm.the_shell.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.NotchAltarBlock;

// dans le temple de la jungle on ne casse et on ne pose rien tant que les 3 epreuves de Notch ne sont pas reussies
// (sinon on creuse jusqu'au tresor). en creatif on fait ce qu'on veut
@EventBusSubscriber(modid = TheShell.MOD_ID)
public class TempleProtection {
    private static final ResourceKey<Structure> TEMPLE = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("jungle_pyramid"));

    private static boolean verrouille(ServerLevel level, BlockPos pos) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(TEMPLE);
        if (structure == null) return false;
        StructureStart temple = level.structureManager().getStructureWithPieceAt(pos, structure);
        if (!temple.isValid()) return false;
        for (BlockPos p : BlockPos.betweenClosed(temple.getBoundingBox().minX(), temple.getBoundingBox().minY(), temple.getBoundingBox().minZ(),
                temple.getBoundingBox().maxX(), temple.getBoundingBox().maxY(), temple.getBoundingBox().maxZ())) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof NotchAltarBlock && !s.getValue(NotchAltarBlock.RESOLU)) return true;
        }
        return false;
    }

    private static boolean refuse(Player player, ServerLevel level, BlockPos pos) {
        if (player.isCreative() || !verrouille(level, pos)) return false;
        player.displayClientMessage(Component.translatable("message.the_shell.temple.locked"), true);
        return true;
    }

    @SubscribeEvent
    public static void casser(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && refuse(event.getPlayer(), level, event.getPos())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void poser(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof Player player && refuse(player, level, event.getPos())) event.setCanceled(true);
    }
}
