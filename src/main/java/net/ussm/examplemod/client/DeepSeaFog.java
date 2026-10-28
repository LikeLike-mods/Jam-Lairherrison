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
import net.ussm.examplemod.worldgen.TestChunkGenerator;

// dans les abysses, plus on descend moins on voit loin (un peu comme l'effet du warden, ca respire au fond)
@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class DeepSeaFog {

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != AtollTravel.ATOLL || event.getCamera().getFluidInCamera() != FogType.WATER) return;
        int top = TestChunkGenerator.ABYSS;
        float t = Mth.clamp((float) (top - event.getCamera().getPosition().y) / (top + 100), 0.0F, 1.0F);
        if (t <= 0) return;
        float far = Mth.lerp((float) Math.sqrt(t), event.getFarPlaneDistance(), 8.0F);
        float time = mc.level.getGameTime() + (float) event.getPartialTick();
        far *= 1.0F + 0.12F * t * Mth.sin(time * 0.08F);
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), far));
        event.setNearPlaneDistance(0.0F);
        event.setCanceled(true);
    }
}
