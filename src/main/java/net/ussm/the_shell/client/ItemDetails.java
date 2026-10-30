package net.ussm.the_shell.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.ussm.the_shell.TheShell;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class ItemDetails {

    @SubscribeEvent
    public static void details(ItemTooltipEvent event) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!id.getNamespace().equals(TheShell.MOD_ID)) return;
        String key = "tooltip.the_shell." + id.getPath() + ".details";
        if (!I18n.exists(key)) return;
        if (!Screen.hasControlDown()) {
            event.getToolTip().add(Component.translatable("tooltip.the_shell.details_hint").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (String line : cutout(I18n.get(key), 42)) event.getToolTip().add(Component.literal(line).withStyle(ChatFormatting.GRAY));
    }

    private static List<String> cutout(String text, int n) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + word.length() + 1 > n) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }
}
