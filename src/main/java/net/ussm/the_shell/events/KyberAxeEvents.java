package net.ussm.the_shell.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.items.KyberAxeItem;
import net.ussm.the_shell.items.LightsaberItem;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class KyberAxeEvents {
    private static boolean felling;

    @SubscribeEvent
    public static void fellTree(BlockEvent.BreakEvent event) {
        if (felling || !(event.getLevel() instanceof ServerLevel level) || !event.getState().is(BlockTags.LOGS)) return;
        ItemStack main = event.getPlayer().getMainHandItem();
        if (!(main.getItem() instanceof KyberAxeItem) || !LightsaberItem.isActive(main) || !KyberAxeItem.fellsWholeTree(main) || event.getPlayer().isShiftKeyDown()) return;
        felling = true;
        try {
            KyberAxeItem.fellTree(level, event.getPos(), event.getPlayer());
        } finally {
            felling = false;
        }
    }

    // kyber tools cannot be repaired the usual ways, only with a kyber crystal in the inventory
    @SubscribeEvent
    public static void noAnvil(AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof LightsaberItem || event.getRight().getItem() instanceof LightsaberItem) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void noGrindstone(GrindstoneEvent.OnPlaceItem event) {
        if (event.getTopItem().getItem() instanceof LightsaberItem || event.getBottomItem().getItem() instanceof LightsaberItem) event.setCanceled(true);
    }
}
