package net.ussm.the_shell.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.items.ConchItem;
import net.ussm.the_shell.items.KyberAxeItem;
import net.ussm.the_shell.items.KyberPickaxeItem;
import net.ussm.the_shell.items.LightsaberItem;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class KyberAxeEvents {
    private static boolean felling;
    private static boolean mining;

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

    @SubscribeEvent
    public static void mineArea(BlockEvent.BreakEvent event) {
        if (mining || !(event.getLevel() instanceof ServerLevel level) || !(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof KyberPickaxeItem) || !LightsaberItem.isActive(main) || player.isShiftKeyDown()
                || !main.isCorrectToolForDrops(event.getState())) return;
        mining = true;
        try {
            KyberPickaxeItem.mineAround(level, player, event.getPos(), event.getState());
        } finally {
            mining = false;
        }
    }

    // kyber stuff can't be repaired the normal way
    @SubscribeEvent
    public static void noAnvil(AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof LightsaberItem || event.getRight().getItem() instanceof LightsaberItem
                || event.getLeft().getItem() instanceof ConchItem || event.getRight().getItem() instanceof ConchItem) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void noGrindstone(GrindstoneEvent.OnPlaceItem event) {
        if (event.getTopItem().getItem() instanceof LightsaberItem || event.getBottomItem().getItem() instanceof LightsaberItem
                || event.getTopItem().getItem() instanceof ConchItem || event.getBottomItem().getItem() instanceof ConchItem) event.setCanceled(true);
    }
}
