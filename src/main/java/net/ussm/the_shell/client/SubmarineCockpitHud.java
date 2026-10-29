package net.ussm.the_shell.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Submarine;

// quand on conduit le sous-marin en vue 1re personne : le cadre du hublot sur les bords, le milieu reste libre
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SubmarineCockpitHud {
    private static final ResourceLocation FRAME = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/gui/submarine_cockpit.png");

    @SubscribeEvent
    public static void draw(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof Submarine) || !mc.options.getCameraType().isFirstPerson()) return;
        GuiGraphics g = event.getGuiGraphics();
        RenderSystem.enableBlend();
        g.blit(FRAME, 0, 0, g.guiWidth(), g.guiHeight(), 0, 0, 320, 180, 320, 180);
        RenderSystem.disableBlend();
    }
}
