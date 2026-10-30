package net.ussm.the_shell.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.items.KyberAxeItem;
import net.ussm.the_shell.items.LightsaberItem;

// une buche cassee avec la hache au kyber allumee : tout l'arbre tombe (accroupi on coupe juste le bloc)
@EventBusSubscriber(modid = TheShell.MOD_ID)
public class KyberAxeEvents {
    private static boolean enCours;

    @SubscribeEvent
    public static void abattre(BlockEvent.BreakEvent event) {
        if (enCours || !(event.getLevel() instanceof ServerLevel level) || !event.getState().is(BlockTags.LOGS)) return;
        ItemStack main = event.getPlayer().getMainHandItem();
        if (!(main.getItem() instanceof KyberAxeItem) || !LightsaberItem.isActive(main) || !KyberAxeItem.abatArbre(main) || event.getPlayer().isShiftKeyDown()) return;
        enCours = true;
        try {
            KyberAxeItem.abattre(level, event.getPos(), event.getPlayer());
        } finally {
            enCours = false;
        }
    }
}
