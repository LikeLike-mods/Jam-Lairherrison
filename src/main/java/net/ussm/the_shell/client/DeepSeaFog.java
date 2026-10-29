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
import net.ussm.the_shell.event.AtollTravel;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

// dans les abysses, plus on descend moins on voit loin. comme l'effet du warden : net jusqu'a une limite puis plus rien
// (le brouillard commence aux 3/4 de la distance, sinon ca fait un voile sur tout l'ecran)
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class DeepSeaFog {

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != AtollTravel.ATOLL || event.getCamera().getFluidInCamera() != FogType.WATER) return;
        int top = TestChunkGenerator.ABYSS;
        float t = Mth.clamp((float) (top - event.getCamera().getPosition().y) / (top + 100), 0.0F, 1.0F);
        if (t <= 0) return;
        float far = Mth.lerp((float) Math.sqrt(t), event.getFarPlaneDistance(), 10.0F);
        float time = mc.level.getGameTime() + (float) event.getPartialTick();
        far *= 1.0F + 0.08F * t * Mth.sin(time * 0.08F);
        far = Math.min(event.getFarPlaneDistance(), far);
        event.setFarPlaneDistance(far);
        event.setNearPlaneDistance(event.getMode() == FogRenderer.FogMode.FOG_SKY ? 0.0F : far * 0.75F);
        event.setCanceled(true);
    }
}
