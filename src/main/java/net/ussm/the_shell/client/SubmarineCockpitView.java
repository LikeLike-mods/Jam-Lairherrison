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
import net.minecraft.world.phys.Vec3;
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
    private static final int METAL = 0xFF46505A, METAL_SOMBRE = 0xFF333A42, CADRE = 0xFF2A2F36, CADRE_CLAIR = 0xFF59636E,
            VERRE = 0x2C8FD3F0, SOL_VERRE = 0x3C7CC2DC;
    // le manche suit la vitesse du sous-marin en douceur
    private static float mancheAvant, mancheCote;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub) || !mc.options.getCameraType().isFirstPerson()) return;
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float yaw = Mth.rotLerp(pt, sub.yRotO, sub.getYRot());
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(TYPE);
        int lumiere = LightTexture.pack(11, 11), plein = LightTexture.FULL_BRIGHT;

        Vec3 v = sub.getDeltaMovement();
        Vec3 devant = Vec3.directionFromRotation(0.0F, yaw);
        float vitesseAvant = (float) Mth.clamp(v.dot(devant) / 0.35, -1.0, 1.0);
        float vitesseCote = (float) Mth.clamp(v.dot(new Vec3(devant.z, 0, -devant.x)) / 0.2, -1.0, 1.0);
        mancheAvant += (vitesseAvant - mancheAvant) * 0.08F;
        mancheCote += (vitesseCote - mancheCote) * 0.08F;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        // le sol : des plaques de metal sous le siege (avec leurs joints), du verre devant pour voir en dessous
        plaques(vc, pose, -1.1F, -1.27F, -1.0F, 1.1F, -1.22F, 0.5F, METAL, lumiere);
        for (float z = -0.9F; z < 0.5F; z += 0.35F) box(vc, pose, -1.1F, -1.22F, z, 1.1F, -1.215F, z + 0.02F, METAL_SOMBRE, lumiere);
        box(vc, pose, -1.1F, -1.27F, 0.5F, 1.1F, -1.25F, 1.7F, SOL_VERRE, lumiere);
        // la bulle
        box(vc, pose, -1.2F, -1.22F, 1.7F, 1.2F, 0.9F, 1.72F, VERRE, lumiere);
        box(vc, pose, -1.22F, -1.22F, -0.4F, -1.2F, 0.9F, 1.7F, VERRE, lumiere);
        box(vc, pose, 1.2F, -1.22F, -0.4F, 1.22F, 0.9F, 1.7F, VERRE, lumiere);
        box(vc, pose, -1.2F, 0.9F, -0.4F, 1.2F, 0.92F, 1.7F, VERRE, lumiere);
        // les montants, avec un liseré clair et des rivets
        for (float x : new float[]{-1.2F, 1.2F}) {
            plaques(vc, pose, x - 0.04F, -1.24F, 1.66F, x + 0.04F, 0.94F, 1.74F, CADRE, lumiere);
            box(vc, pose, x - 0.045F, -1.24F, 1.655F, x + 0.045F, 0.94F, 1.665F, CADRE_CLAIR, lumiere);
            plaques(vc, pose, x - 0.04F, 0.88F, -0.4F, x + 0.04F, 0.96F, 1.74F, CADRE, lumiere);
            box(vc, pose, x - 0.04F, -1.24F, -0.4F, x + 0.04F, -1.16F, 1.74F, CADRE, lumiere);
            for (float y = -1.0F; y < 0.9F; y += 0.4F) box(vc, pose, x - 0.05F, y, 1.64F, x + 0.05F, y + 0.03F, 1.66F, CADRE_CLAIR, lumiere);
        }
        box(vc, pose, -1.24F, 0.88F, 1.66F, 1.24F, 0.96F, 1.74F, CADRE, lumiere);
        box(vc, pose, -1.24F, -1.24F, 1.66F, 1.24F, -1.16F, 1.74F, CADRE, lumiere);
        box(vc, pose, -1.24F, 0.88F, 0.56F, 1.24F, 0.96F, 0.64F, CADRE, lumiere);
        box(vc, pose, -1.24F, 0.875F, 0.55F, 1.24F, 0.885F, 0.65F, CADRE_CLAIR, lumiere);
        // derriere : le mur de la cabine avec la trappe qui descend
        plaques(vc, pose, -1.2F, -1.22F, -0.42F, 1.2F, 0.92F, -0.38F, METAL, lumiere);
        box(vc, pose, -0.44F, -1.22F, -0.375F, 0.44F, 0.24F, -0.365F, CADRE_CLAIR, lumiere);
        box(vc, pose, -0.4F, -1.22F, -0.37F, 0.4F, 0.2F, -0.36F, 0xFF1C2024, lumiere);

        // le tableau de bord : un caisson avec un rebord clair et une facade plus sombre
        plaques(vc, pose, -0.7F, -0.95F, 1.05F, 0.7F, -0.6F, 1.45F, 0xFF2F353D, lumiere);
        box(vc, pose, -0.72F, -0.6F, 1.03F, 0.72F, -0.57F, 1.47F, CADRE_CLAIR, lumiere);
        box(vc, pose, -0.66F, -0.92F, 1.045F, 0.66F, -0.63F, 1.05F, 0xFF22272D, lumiere);
        box(vc, pose, -0.23F, -0.79F, 1.035F, 0.23F, -0.63F, 1.045F, 0xFF101418, lumiere);
        box(vc, pose, -0.21F, -0.77F, 1.03F, 0.21F, -0.65F, 1.035F, 0xFF2BAFD8, plein);
        float t = sub.tickCount + pt;
        for (int i = 0; i < 4; i++) {
            float y = -0.755F + i * 0.028F + (float) Math.sin(t * 0.2 + i) * 0.004F;
            float larg = 0.06F + 0.12F * (0.5F + 0.5F * (float) Math.sin(t * 0.13 + i * 1.7));
            box(vc, pose, -0.19F, y, 1.028F, -0.19F + larg, y + 0.01F, 1.03F, 0xFF9FF4FF, plein);
        }
        float[] aiguilles = {(float) Math.sin(t * 0.07) * 0.9F + (float) Math.sin(t * 0.31) * 0.15F, 1.2F - 2.4F * sub.getEnergy() / Submarine.MAX_ENERGY};
        float[] places = {-0.45F, 0.45F};
        for (int i = 0; i < 2; i++) {
            box(vc, pose, places[i] - 0.13F, -0.87F, 1.04F, places[i] + 0.13F, -0.61F, 1.045F, CADRE_CLAIR, lumiere);
            box(vc, pose, places[i] - 0.11F, -0.85F, 1.035F, places[i] + 0.11F, -0.63F, 1.04F, 0xFFE6E2D4, plein);
            for (int k = -2; k <= 2; k++) {
                pose.pushPose();
                pose.translate(places[i], -0.74F, 1.034F);
                pose.mulPose(Axis.ZP.rotation(k * 0.5F));
                box(vc, pose, -0.004F, 0.08F, -0.002F, 0.004F, 0.1F, 0.0F, 0xFF2A2A2A, plein);
                pose.popPose();
            }
            pose.pushPose();
            pose.translate(places[i], -0.74F, 1.03F);
            pose.mulPose(Axis.ZP.rotation(aiguilles[i]));
            box(vc, pose, -0.007F, 0.0F, -0.004F, 0.007F, 0.095F, 0.0F, i == 0 ? 0xFF2A2A2A : 0xFFD8402C, plein);
            pose.popPose();
            box(vc, pose, places[i] - 0.015F, -0.755F, 1.026F, places[i] + 0.015F, -0.725F, 1.03F, 0xFF555555, plein);
        }
        int[] voyants = {0xFF4FE07A, 0xFFE6B53A, 0xFFE0482F, 0xFF4FA8F0};
        for (int i = 0; i < 4; i++) {
            float x = -0.6F + i * 0.06F;
            boolean allume = i != 2 || (sub.tickCount / 10) % 2 == 0;
            box(vc, pose, x - 0.02F, -0.9F, 1.04F, x + 0.02F, -0.86F, 1.045F, allume ? voyants[i] : 0xFF30343A, plein);
        }
        // le manche : un socle, une tige, une poignee avec ses bagues et un bouton rouge dessus
        pose.pushPose();
        pose.translate(0.0F, -1.22F, 0.55F);
        box(vc, pose, -0.09F, 0.0F, -0.09F, 0.09F, 0.03F, 0.09F, METAL_SOMBRE, lumiere);
        box(vc, pose, -0.05F, 0.03F, -0.05F, 0.05F, 0.06F, 0.05F, 0xFF1E2226, lumiere);
        pose.mulPose(Axis.XP.rotationDegrees(mancheAvant * 22.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-mancheCote * 22.0F));
        box(vc, pose, -0.02F, 0.06F, -0.02F, 0.02F, 0.36F, 0.02F, 0xFF8C949C, lumiere);
        box(vc, pose, -0.04F, 0.36F, -0.04F, 0.04F, 0.52F, 0.04F, 0xFF22262B, lumiere);
        for (float y = 0.39F; y < 0.52F; y += 0.04F) box(vc, pose, -0.043F, y, -0.043F, 0.043F, y + 0.012F, 0.043F, 0xFF3A4048, lumiere);
        box(vc, pose, -0.03F, 0.52F, -0.03F, 0.03F, 0.545F, 0.03F, 0xFFB02226, lumiere);
        box(vc, pose, -0.015F, 0.545F, -0.015F, 0.015F, 0.555F, 0.015F, 0xFFE8524E, plein);
        pose.popPose();
        pose.popPose();
        buffers.endBatch(TYPE);
    }

    // une surface en metal decoupee en petites plaques, chacune d'un gris un tout petit peu different
    private static void plaques(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        float pas = 0.18F;
        int nx = Math.max(1, Math.round((x1 - x0) / pas)), ny = Math.max(1, Math.round((y1 - y0) / pas)), nz = Math.max(1, Math.round((z1 - z0) / pas));
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    int h = (i * 73856093) ^ (j * 19349663) ^ (k * 83492791) ^ argb;
                    float f = 0.9F + ((h >>> 8) & 255) / 255.0F * 0.18F;
                    box(vc, pose, x0 + (x1 - x0) * i / nx, y0 + (y1 - y0) * j / ny, z0 + (z1 - z0) * k / nz,
                            x0 + (x1 - x0) * (i + 1) / nx, y0 + (y1 - y0) * (j + 1) / ny, z0 + (z1 - z0) * (k + 1) / nz, teinte(argb, f), light);
                }
            }
        }
    }

    // chaque face a sa propre lumiere (dessus clair, dessous sombre) pour que ca ait du relief
    private static void box(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        PoseStack.Pose p = pose.last();
        quad(vc, p, teinte(argb, 0.55F), light, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(vc, p, teinte(argb, 1.0F), light, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        quad(vc, p, teinte(argb, 0.8F), light, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(vc, p, teinte(argb, 0.8F), light, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        quad(vc, p, teinte(argb, 0.68F), light, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        quad(vc, p, teinte(argb, 0.68F), light, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static int teinte(int argb, float f) {
        int r = Math.min(255, (int) (((argb >> 16) & 255) * f)), g = Math.min(255, (int) (((argb >> 8) & 255) * f)), b = Math.min(255, (int) ((argb & 255) * f));
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose p, int argb, int light, float nx, float ny, float nz, float... v) {
        for (int i = 0; i < 4; i++) {
            vc.addVertex(p, v[i * 3], v[i * 3 + 1], v[i * 3 + 2]).setColor(argb).setUv(i == 1 || i == 2 ? 1 : 0, i >= 2 ? 1 : 0)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
        }
    }
}
