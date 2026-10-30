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

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SubmarineCockpitHud {
    private static java.util.concurrent.CompletableFuture<int[]> search;
    private static int[] rift;
    private static long last;

    @SubscribeEvent
    public static void draw(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub)) return;
        GuiGraphics g = event.getGuiGraphics();
        float energy = sub.getEnergy() / (float) Submarine.MAX_ENERGY;
        int w = 60, x = g.guiWidth() - w - 8, y = g.guiHeight() - 22;
        g.fill(x - 2, y - 2, x + w + 2, y + 8, 0xFF2B3036);
        g.fill(x, y, x + w, y + 6, 0xFF0D1114);
        int colour = energy > 0.25F ? 0xFF5FE07A : energy > 0 && (mc.player.tickCount / 6) % 2 == 0 ? 0xFFE0482F : 0xFF7A1A12;
        g.fill(x + 1, y + 1, x + 1 + Math.round((w - 2) * energy), y + 5, colour);
        g.drawString(mc.font, Component.translatable("hud.the_shell.submarine.energy", Math.round(energy * 100)), x, y - 11, 0xFFD8E2E8);
        if (sub.getTier() >= 3) sonar(mc, g, sub, x + w, y - 24);
        if (!mc.player.getData(net.ussm.the_shell.init.ModAttachments.CABIN_BUTTON_USED) && SubmarineCockpitView.seeButton(mc, sub)) {
            Component help = Component.translatable("hud.the_shell.submarine.cabin_button");
            g.drawString(mc.font, help, (g.guiWidth() - mc.font.width(help)) / 2, g.guiHeight() / 2 + 12, 0xFFFFE27A);
        }
    }

    private static void sonar(Minecraft mc, GuiGraphics g, Submarine sub, int right, int y) {
        long now = mc.level.getGameTime();
        if (search == null && now - last > 60) {
            int sx = sub.getBlockX(), sz = sub.getBlockZ();
            search = java.util.concurrent.CompletableFuture.supplyAsync(() -> net.ussm.the_shell.events.SubmarineInterior.nearestRift(sx, sz));
            last = now;
        }
        if (search != null && search.isDone()) {
            rift = search.getNow(null);
            search = null;
        }
        Component txt;
        if (rift == null) {
            txt = Component.translatable("hud.the_shell.sonar.none");
        } else {
            double dx = rift[0] + 0.5 - sub.getX(), dz = rift[1] + 0.5 - sub.getZ();
            float head = (float) Math.toDegrees(Math.atan2(-dx, dz));
            int i = Math.floorMod(Math.round((head - mc.player.getYRot()) / 45.0F), 8);
            String[] arrows = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};
            txt = Component.translatable("hud.the_shell.sonar", (int) Math.sqrt(dx * dx + dz * dz), arrows[i]);
        }
        g.drawString(mc.font, txt, right - mc.font.width(txt), y, 0xFF7FE3FF);
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof Submarine) event.setCanceled(true);
    }
}
