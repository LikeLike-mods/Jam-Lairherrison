package net.ussm.the_shell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.events.AtollTravel;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class DeepSeaFog {

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || event.getCamera().getFluidInCamera() != FogType.WATER) return;
        if (mc.player != null && (mc.player.isCreative() || mc.player.isSpectator())) return;
        double y = event.getCamera().getPosition().y;
        float far;
        if (mc.level.dimension() == net.ussm.the_shell.events.AbyssTravel.ABYSS) {
            float d = Mth.clamp((float) (net.ussm.the_shell.worldgen.AbyssTerrain.CEILING - y) / 190F, 0.0F, 1.0F);
            far = Mth.lerp(d, 56.0F, 32.0F);
        } else {
            float t = profondeur(mc, y);
            if (t <= 0) return;
            far = Math.min(event.getFarPlaneDistance(), Mth.lerp((float) Math.sqrt(t), event.getFarPlaneDistance(), 28.0F));
        }
        float time = mc.level.getGameTime() + (float) event.getPartialTick();
        far *= 1.0F + 0.05F * Mth.sin(time * 0.08F);
        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(event.getMode() == FogRenderer.FogMode.FOG_SKY ? 0.0F : far * 0.3F);
        event.setCanceled(true);
    }

    private static float profondeur(Minecraft mc, double y) {
        if (mc.level.dimension() == AtollTravel.ATOLL) {
            int top = TestChunkGenerator.ABYSS;
            return Mth.clamp((float) (top - y) / (top + 100), 0.0F, 1.0F);
        }
        if (mc.level.dimension() == net.ussm.the_shell.events.AbyssTravel.ABYSS) {
            return 0.8F + 0.2F * Mth.clamp((float) (net.ussm.the_shell.worldgen.AbyssTerrain.CEILING - y) / 190F, 0.0F, 1.0F);
        }
        return 0.0F;
    }

    @SubscribeEvent
    public static void couleur(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || event.getCamera().getFluidInCamera() != FogType.WATER) return;
        float t = profondeur(mc, event.getCamera().getPosition().y);
        if (t <= 0) return;
        float k = 1.0F - Mth.clamp(t * 1.4F, 0.0F, 1.0F);
        event.setRed(event.getRed() * k);
        event.setGreen(event.getGreen() * k);
        event.setBlue(event.getBlue() * k);
    }
}
