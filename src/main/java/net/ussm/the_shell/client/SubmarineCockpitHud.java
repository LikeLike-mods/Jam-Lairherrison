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
    // le sonar (niveau netherite) cherche la faille la plus proche en tache de fond, toutes les 3 secondes
    private static java.util.concurrent.CompletableFuture<int[]> recherche;
    private static int[] faille;
    private static long derniere;

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
        if (sub.getTier() >= 3) sonar(mc, g, sub, x + w, y - 24);
        if (SubmarineCockpitView.regardeBouton(mc, sub)) {
            Component aide = Component.translatable("hud.the_shell.submarine.cabin_button");
            g.drawString(mc.font, aide, (g.guiWidth() - mc.font.width(aide)) / 2, g.guiHeight() / 2 + 12, 0xFFFFE27A);
        }
    }

    private static void sonar(Minecraft mc, GuiGraphics g, Submarine sub, int droite, int y) {
        long now = mc.level.getGameTime();
        if (recherche == null && now - derniere > 60) {
            int sx = sub.getBlockX(), sz = sub.getBlockZ();
            recherche = java.util.concurrent.CompletableFuture.supplyAsync(() -> chercher(sx, sz));
            derniere = now;
        }
        if (recherche != null && recherche.isDone()) {
            faille = recherche.getNow(null);
            recherche = null;
        }
        Component texte;
        if (faille == null) {
            texte = Component.translatable("hud.the_shell.sonar.none");
        } else {
            double dx = faille[0] + 0.5 - sub.getX(), dz = faille[1] + 0.5 - sub.getZ();
            float cap = (float) Math.toDegrees(Math.atan2(-dx, dz));
            int i = Math.floorMod(Math.round((cap - mc.player.getYRot()) / 45.0F), 8);
            String[] fleches = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};
            texte = Component.translatable("hud.the_shell.sonar", (int) Math.sqrt(dx * dx + dz * dz), fleches[i]);
        }
        g.drawString(mc.font, texte, droite - mc.font.width(texte), y, 0xFF7FE3FF);
    }

    // on balaie en carres de plus en plus grands (pas de 8 blocs) jusqu'a 600 blocs
    private static int[] chercher(int x, int z) {
        for (int r = 0; r <= 600; r += 8) {
            for (int dx = -r; dx <= r; dx += 8) {
                for (int dz = -r; dz <= r; dz += 8) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    if (net.ussm.the_shell.worldgen.TestChunkGenerator.isRift(x + dx, z + dz)) return new int[]{x + dx, z + dz};
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof Submarine) event.setCanceled(true);
    }
}
