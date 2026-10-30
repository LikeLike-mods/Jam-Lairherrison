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

// ctrl en survolant un objet du mod : sa description detaillee (cle tooltip.the_shell.<objet>.details dans les fichiers de langue)
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class ItemDetails {

    @SubscribeEvent
    public static void details(ItemTooltipEvent event) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!id.getNamespace().equals(TheShell.MOD_ID)) return;
        String cle = "tooltip.the_shell." + id.getPath() + ".details";
        if (!I18n.exists(cle)) return;
        if (!Screen.hasControlDown()) {
            event.getToolTip().add(Component.translatable("tooltip.the_shell.details_hint").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (String ligne : decoupe(I18n.get(cle), 42)) event.getToolTip().add(Component.literal(ligne).withStyle(ChatFormatting.GRAY));
    }

    // coupe le texte en lignes d'environ n caracteres, sans couper les mots
    private static List<String> decoupe(String texte, int n) {
        List<String> lignes = new ArrayList<>();
        StringBuilder ligne = new StringBuilder();
        for (String mot : texte.split(" ")) {
            if (ligne.length() > 0 && ligne.length() + mot.length() + 1 > n) {
                lignes.add(ligne.toString());
                ligne.setLength(0);
            }
            if (ligne.length() > 0) ligne.append(' ');
            ligne.append(mot);
        }
        if (ligne.length() > 0) lignes.add(ligne.toString());
        return lignes;
    }
}
