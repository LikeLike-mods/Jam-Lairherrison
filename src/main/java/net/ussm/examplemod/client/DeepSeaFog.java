package net.ussm.examplemod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.event.AtollTravel;

// sous l'eau dans les abysses, plus on descend plus c'est noir et moins on voit loin
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class DeepSeaFog {

    private static float depth(ViewportEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != AtollTravel.ATOLL) return 0;
        if (event.getCamera().getFluidInCamera() != FogType.WATER) return 0;
        return Mth.clamp((float) -event.getCamera().getPosition().y / 128.0F, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog event) {
        float t = depth(event);
        if (t <= 0) return;
        float far = Mth.lerp((float) Math.sqrt(t), event.getFarPlaneDistance(), 6.0F);
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), far));
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), -4.0F));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void color(ViewportEvent.ComputeFogColor event) {
        float t = depth(event);
        if (t <= 0) return;
        float k = 1.0F - 0.9F * t;
        event.setRed(event.getRed() * k);
        event.setGreen(event.getGreen() * k);
        event.setBlue(event.getBlue() * k);
    }
}
