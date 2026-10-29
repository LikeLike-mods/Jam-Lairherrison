package net.ussm.the_shell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Submarine;

// le cockpit vu de l'interieur : une bulle de verre avec ses montants, le sol en verre devant, le tableau de bord et le manche.
// il n'existe que dans la tete du pilote (vue a la 1re personne), dehors on voit le sous-marin normal
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SubmarineCockpitView {
    private static final RenderType TYPE = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/misc/white.png"));
    private static final int METAL = 0xFF3C434C, CADRE = 0xFF262A30, VERRE = 0x2C8FD3F0, SOL_VERRE = 0x3C7CC2DC;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub) || !mc.options.getCameraType().isFirstPerson()) return;
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(TYPE);
        int lumiere = LightTexture.pack(11, 11), plein = LightTexture.FULL_BRIGHT;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(pt, sub.yRotO, sub.getYRot())));
        // le sol : du metal sous le siege, du verre devant pour voir en dessous
        box(vc, pose, -1.1F, -1.27F, -1.0F, 1.1F, -1.22F, 0.5F, METAL, lumiere);
        box(vc, pose, -1.1F, -1.27F, 0.5F, 1.1F, -1.25F, 1.7F, SOL_VERRE, lumiere);
        // la bulle
        box(vc, pose, -1.2F, -1.22F, 1.7F, 1.2F, 0.9F, 1.72F, VERRE, lumiere);
        box(vc, pose, -1.22F, -1.22F, -0.4F, -1.2F, 0.9F, 1.7F, VERRE, lumiere);
        box(vc, pose, 1.2F, -1.22F, -0.4F, 1.22F, 0.9F, 1.7F, VERRE, lumiere);
        box(vc, pose, -1.2F, 0.9F, -0.4F, 1.2F, 0.92F, 1.7F, VERRE, lumiere);
        // les montants
        for (float x : new float[]{-1.2F, 1.2F}) {
            box(vc, pose, x - 0.04F, -1.24F, 1.66F, x + 0.04F, 0.94F, 1.74F, CADRE, lumiere);
            box(vc, pose, x - 0.04F, 0.88F, -0.4F, x + 0.04F, 0.96F, 1.74F, CADRE, lumiere);
            box(vc, pose, x - 0.04F, -1.24F, -0.4F, x + 0.04F, -1.16F, 1.74F, CADRE, lumiere);
        }
        box(vc, pose, -1.24F, 0.88F, 1.66F, 1.24F, 0.96F, 1.74F, CADRE, lumiere);
        box(vc, pose, -1.24F, -1.24F, 1.66F, 1.24F, -1.16F, 1.74F, CADRE, lumiere);
        box(vc, pose, -1.24F, 0.88F, 0.56F, 1.24F, 0.96F, 0.64F, CADRE, lumiere);
        // derriere : le mur de la cabine avec la trappe qui descend
        box(vc, pose, -1.2F, -1.22F, -0.42F, 1.2F, 0.92F, -0.38F, METAL, lumiere);
        box(vc, pose, -0.4F, -1.22F, -0.37F, 0.4F, 0.2F, -0.36F, 0xFF1C2024, lumiere);

        // le tableau de bord
        box(vc, pose, -0.7F, -0.95F, 1.05F, 0.7F, -0.6F, 1.45F, 0xFF2A2F36, lumiere);
        box(vc, pose, -0.22F, -0.78F, 1.03F, 0.22F, -0.64F, 1.05F, 0xFF46D8FF, plein);
        float t = sub.tickCount + pt;
        float[] aiguilles = {(float) Math.sin(t * 0.07) * 0.9F + (float) Math.sin(t * 0.31) * 0.15F, 1.2F - 2.4F * sub.getEnergy() / Submarine.MAX_ENERGY};
        float[] places = {-0.45F, 0.45F};
        for (int i = 0; i < 2; i++) {
            box(vc, pose, places[i] - 0.12F, -0.86F, 1.035F, places[i] + 0.12F, -0.62F, 1.05F, 0xFF14181C, lumiere);
            pose.pushPose();
            pose.translate(places[i], -0.74F, 1.03F);
            pose.mulPose(Axis.ZP.rotation(aiguilles[i]));
            box(vc, pose, -0.008F, 0.0F, -0.006F, 0.008F, 0.1F, 0.0F, i == 0 ? 0xFFF0F0F0 : 0xFFF06040, plein);
            pose.popPose();
        }
        int[] voyants = {0xFF4FE07A, 0xFFE6B53A, 0xFFE0482F, 0xFF4FA8F0};
        for (int i = 0; i < 4; i++) {
            float x = -0.18F + i * 0.12F;
            boolean allume = i != 2 || (sub.tickCount / 10) % 2 == 0;
            box(vc, pose, x - 0.02F, -0.9F, 1.035F, x + 0.02F, -0.86F, 1.05F, allume ? voyants[i] : 0xFF30343A, plein);
        }
        // le manche, il penche avec les touches
        float avant = mc.player.zza, cote = mc.player.xxa;
        pose.pushPose();
        pose.translate(0.0F, -1.22F, 0.55F);
        pose.mulPose(Axis.XP.rotationDegrees(avant * 18.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-cote * 18.0F));
        box(vc, pose, -0.025F, 0.0F, -0.025F, 0.025F, 0.45F, 0.025F, 0xFF30343A, lumiere);
        box(vc, pose, -0.05F, 0.45F, -0.05F, 0.05F, 0.56F, 0.05F, 0xFFC8282C, lumiere);
        pose.popPose();
        pose.popPose();
        buffers.endBatch(TYPE);
    }

    private static void box(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        PoseStack.Pose p = pose.last();
        quad(vc, p, argb, light, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(vc, p, argb, light, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        quad(vc, p, argb, light, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(vc, p, argb, light, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        quad(vc, p, argb, light, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        quad(vc, p, argb, light, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p, int argb, int light, float nx, float ny, float nz, float... v) {
        for (int i = 0; i < 4; i++) {
            vc.addVertex(p, v[i * 3], v[i * 3 + 1], v[i * 3 + 2]).setColor(argb).setUv(i == 1 || i == 2 ? 1 : 0, i >= 2 ? 1 : 0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
        }
    }
}
