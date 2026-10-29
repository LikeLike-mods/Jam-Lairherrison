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
import net.ussm.the_shell.entity.Submarine;
import net.ussm.the_shell.event.DeepSeaEvents;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

// avec le casque de scaphandre : on voit a travers le hublot, et un manometre monte avec la pression
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
        float fill = Mth.clamp(depth / MAX_DEPTH, 0.0F, 1.0F);
        boolean safe = DeepSeaEvents.hasFullSuit(player) || player.getVehicle() instanceof Submarine;

        // manometre a droite, dans le noir autour du hublot
        int x = w - 22;
        int top = h / 5;
        int bottom = h - h / 5;
        g.fill(x - 3, top - 3, x + 11, bottom + 3, 0xFF6B4A22);
        g.fill(x - 2, top - 2, x + 10, bottom + 2, 0xFFC18B47);
        g.fill(x, top, x + 8, bottom, 0xFF0D1114);
        int level = bottom - Math.round((bottom - top) * fill);
        int color = fill < 0.4F ? 0xFF4FD16A : fill < 0.7F ? 0xFFE3C13B : 0xFFE0482F;
        if (!safe && fill >= 0.7F && (player.tickCount / 5) % 2 == 0) color = 0xFF7A1A12;
        g.fill(x + 1, level, x + 7, bottom, color);
        // traits aux paliers de pression
        int abyss = TestChunkGenerator.ABYSS;
        for (int y : new int[]{abyss, abyss - 40, abyss - 80}) {
            float f = Mth.clamp((TestChunkGenerator.SEA - y) / MAX_DEPTH, 0.0F, 1.0F);
            int ty = bottom - Math.round((bottom - top) * f);
            g.fill(x - 2, ty, x + 10, ty + 1, 0xFF6B4A22);
        }
        String bar = String.format("%.1f", 1.0F + depth / 10.0F);
        g.drawCenteredString(mc.font, bar, x + 4, bottom + 6, 0xFFE9C58A);
        g.drawCenteredString(mc.font, "bar", x + 4, bottom + 16, 0xFFB08A55);
        RenderSystem.disableBlend();
    }
}
