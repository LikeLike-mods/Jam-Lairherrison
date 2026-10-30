package net.ussm.the_shell.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.events.DeepSeaEvents;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class DivingHud {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/misc/diving_helmet.png");
    private static final float MAX_DEPTH = TestChunkGenerator.SEA + 100.0F;

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "diving_helmet"), DivingHud::render);
    }

    private static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || !mc.options.getCameraType().isFirstPerson()) return;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModItems.DIVING_HELMET.get()) && !head.is(ModItems.DIVING_ABYSS_HELMET.get())) return;

        int w = g.guiWidth();
        int h = g.guiHeight();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        g.blit(OVERLAY, 0, 0, -90, 0.0F, 0.0F, w, h, w, h);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        float depth = player.isEyeInFluid(FluidTags.WATER) ? Math.max(0.0F, player.level().getSeaLevel() - (float) player.getEyeY()) : 0.0F;
        if (depth > 0 && player.level().dimension() == net.ussm.the_shell.events.AbyssTravel.ABYSS) depth += MAX_DEPTH;
        // la jauge suit le palier de pression (0 a 5)
        int palier = DeepSeaEvents.pressureLevel(player, player.isInWater());
        float fill = palier / 5.0F;
        boolean safe = DeepSeaEvents.hasFullSuit(player) || player.getVehicle() instanceof Submarine;

        int x = w - 22;
        int top = h / 5;
        int bottom = h - h / 5;
        g.fill(x - 3, top - 3, x + 11, bottom + 3, 0xFF6B4A22);
        g.fill(x - 2, top - 2, x + 10, bottom + 2, 0xFFC18B47);
        g.fill(x, top, x + 8, bottom, 0xFF0D1114);
        int level = bottom - Math.round((bottom - top) * fill);
        int color = palier <= 1 ? 0xFF4FD16A : palier == 2 ? 0xFFE3C13B : palier == 3 ? 0xFFE08A2F : 0xFFE0482F;
        if (!safe && palier >= 3 && (player.tickCount / 5) % 2 == 0) color = 0xFF7A1A12;
        g.fill(x + 1, level, x + 7, bottom, color);
        for (int k = 1; k < 5; k++) {
            int ty = bottom - Math.round((bottom - top) * k / 5.0F);
            g.fill(x - 2, ty, x + 10, ty + 1, 0xFF6B4A22);
        }
        // la bouteille d'oxygene, a gauche du manometre
        float o2 = player.getData(net.ussm.the_shell.init.ModAttachments.OXYGEN) / (float) net.ussm.the_shell.init.ModAttachments.MAX_OXYGEN;
        int ox = x - 18;
        g.fill(ox - 3, top - 3, ox + 9, bottom + 3, 0xFF2E4A5A);
        g.fill(ox - 2, top - 2, ox + 8, bottom + 2, 0xFF6FA9C2);
        g.fill(ox, top, ox + 6, bottom, 0xFF0D1114);
        int o2Color = o2 > 0.25F ? 0xFF5FD3F0 : (player.tickCount / 5) % 2 == 0 ? 0xFFE0482F : 0xFF7A1A12;
        g.fill(ox + 1, bottom - Math.round((bottom - top) * o2), ox + 5, bottom, o2Color);
        g.drawCenteredString(mc.font, "O2", ox + 3, bottom + 6, 0xFF9FD8EA);
        g.drawCenteredString(mc.font, Math.round(o2 * 100) + "%", ox + 3, bottom + 16, 0xFF6FA9C2);
        String bar = String.format("%.1f", 1.0F + depth / 10.0F);
        g.drawCenteredString(mc.font, bar, x + 4, bottom + 6, 0xFFE9C58A);
        g.drawCenteredString(mc.font, "bar", x + 4, bottom + 16, 0xFFB08A55);
        RenderSystem.disableBlend();
    }
}
