package net.ussm.the_shell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.events.AbyssTravel;
import net.ussm.the_shell.events.AtollTravel;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

// passage dans les failles : l'ecran fond au noir quand on approche du bout, et revient doucement de l'autre cote.
// c'est la profondeur dans la faille qui decide, donc l'arrivee commence deja dans le noir et s'eclaircit en continuant
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class RiftFade {
    private static float noir;

    @SubscribeEvent
    public static void draw(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        float cible = 0.0F;
        double y = mc.player.getEyeY();
        if (TestChunkGenerator.isRift(mc.player.getBlockX(), mc.player.getBlockZ())) {
            if (mc.level.dimension() == AtollTravel.ATOLL) {
                // de 22 blocs au-dessus du passage jusqu'au passage
                cible = Mth.clamp((float) ((TestChunkGenerator.RIFT_DOWN + 22 - y) / 22.0), 0.0F, 1.0F);
            } else if (mc.level.dimension() == AbyssTravel.ABYSS) {
                cible = Mth.clamp((float) ((y - (AbyssTravel.UP - 24)) / 24.0), 0.0F, 1.0F);
            }
        }
        noir += (cible - noir) * 0.15F;
        if (noir < 0.01F) return;
        GuiGraphics g = event.getGuiGraphics();
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), ((int) (Mth.clamp(noir, 0.0F, 1.0F) * 255) << 24));
    }
}
