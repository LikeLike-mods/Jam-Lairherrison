package net.ussm.the_shell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Submarine;

// aux commandes du sous-marin : la batterie en bas a droite, et pas de main qui traine devant la vitre
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SubmarineCockpitHud {

    @SubscribeEvent
    public static void draw(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub)) return;
        GuiGraphics g = event.getGuiGraphics();
        float energy = sub.getEnergy() / (float) Submarine.MAX_ENERGY;
        int w = 60, x = g.guiWidth() - w - 8, y = g.guiHeight() - 22;
        g.fill(x - 2, y - 2, x + w + 2, y + 8, 0xFF2B3036);
        g.fill(x, y, x + w, y + 6, 0xFF0D1114);
        int color = energy > 0.25F ? 0xFF5FE07A : energy > 0 && (mc.player.tickCount / 6) % 2 == 0 ? 0xFFE0482F : 0xFF7A1A12;
        g.fill(x + 1, y + 1, x + 1 + Math.round((w - 2) * energy), y + 5, color);
        g.drawString(mc.font, Component.translatable("hud.the_shell.submarine.energy", Math.round(energy * 100)), x, y - 11, 0xFFD8E2E8);
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof Submarine) event.setCanceled(true);
    }
}
