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

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SubmarineCockpitView {
    private static final RenderType TYPE = RenderType.entityTranslucent(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/misc/white.png"));
    private static final int METAL = 0xFF46505A, METAL_DARK = 0xFF333A42, FRAME = 0xFF2A2F36, FRAME_LIGHT = 0xFF59636E,
            GLASS = 0x2C8FD3F0, GLASS_GROUND = 0x3C7CC2DC;
    private static float leverY, leverX;

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
        int light = LightTexture.pack(11, 11), plein = LightTexture.FULL_BRIGHT;

        Vec3 v = sub.getDeltaMovement();
        Vec3 forward = Vec3.directionFromRotation(0.0F, yaw);
        float speedY = (float) Mth.clamp(v.dot(forward) / 0.35, -1.0, 1.0);
        float speedX = (float) Mth.clamp(v.dot(new Vec3(forward.z, 0, -forward.x)) / 0.2, -1.0, 1.0);
        leverY += (speedY - leverY) * 0.08F;
        leverX += (speedX - leverX) * 0.08F;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        plates(vc, pose, -1.1F, -1.27F, -1.0F, 1.1F, -1.22F, 0.5F, METAL, light);
        for (float z = -0.9F; z < 0.5F; z += 0.35F) box(vc, pose, -1.1F, -1.22F, z, 1.1F, -1.215F, z + 0.02F, METAL_DARK, light);
        box(vc, pose, -1.1F, -1.27F, 0.5F, 1.1F, -1.25F, 1.7F, GLASS_GROUND, light);
        box(vc, pose, -1.2F, -1.22F, 1.7F, 1.2F, 0.9F, 1.72F, GLASS, light);
        box(vc, pose, -1.22F, -1.22F, -0.4F, -1.2F, 0.9F, 1.7F, GLASS, light);
        box(vc, pose, 1.2F, -1.22F, -0.4F, 1.22F, 0.9F, 1.7F, GLASS, light);
        box(vc, pose, -1.2F, 0.9F, -0.4F, 1.2F, 0.92F, 1.7F, GLASS, light);
        for (float x : new float[]{-1.2F, 1.2F}) {
            plates(vc, pose, x - 0.04F, -1.24F, 1.66F, x + 0.04F, 0.94F, 1.74F, FRAME, light);
            box(vc, pose, x - 0.045F, -1.24F, 1.655F, x + 0.045F, 0.94F, 1.665F, FRAME_LIGHT, light);
            plates(vc, pose, x - 0.04F, 0.88F, -0.4F, x + 0.04F, 0.96F, 1.74F, FRAME, light);
            box(vc, pose, x - 0.04F, -1.24F, -0.4F, x + 0.04F, -1.16F, 1.74F, FRAME, light);
            for (float y = -1.0F; y < 0.9F; y += 0.4F) box(vc, pose, x - 0.05F, y, 1.64F, x + 0.05F, y + 0.03F, 1.66F, FRAME_LIGHT, light);
        }
        box(vc, pose, -1.24F, 0.88F, 1.66F, 1.24F, 0.96F, 1.74F, FRAME, light);
        box(vc, pose, -1.24F, -1.24F, 1.66F, 1.24F, -1.16F, 1.74F, FRAME, light);
        box(vc, pose, -1.24F, 0.88F, 0.56F, 1.24F, 0.96F, 0.64F, FRAME, light);
        box(vc, pose, -1.24F, 0.875F, 0.55F, 1.24F, 0.885F, 0.65F, FRAME_LIGHT, light);
        plates(vc, pose, -1.2F, -1.22F, -0.42F, 1.2F, 0.92F, -0.38F, METAL, light);
        box(vc, pose, -0.44F, -1.22F, -0.375F, 0.44F, 0.24F, -0.365F, FRAME_LIGHT, light);
        box(vc, pose, -0.4F, -1.22F, -0.37F, 0.4F, 0.2F, -0.36F, 0xFF1C2024, light);

        plates(vc, pose, -0.7F, -0.95F, 1.05F, 0.7F, -0.6F, 1.45F, 0xFF2F353D, light);
        box(vc, pose, -0.72F, -0.6F, 1.03F, 0.72F, -0.57F, 1.47F, FRAME_LIGHT, light);
        box(vc, pose, -0.66F, -0.92F, 1.045F, 0.66F, -0.63F, 1.05F, 0xFF22272D, light);
        box(vc, pose, -0.23F, -0.79F, 1.035F, 0.23F, -0.63F, 1.045F, 0xFF101418, light);
        box(vc, pose, -0.21F, -0.77F, 1.03F, 0.21F, -0.65F, 1.035F, 0xFF2BAFD8, plein);
        float t = sub.tickCount + pt;
        for (int i = 0; i < 2; i++) {
            float width = 0.06F + 0.24F * (0.5F + 0.5F * (float) Math.sin(t * 0.13 + i * 1.7));
            box(vc, pose, -6 * P, -24 * P + i * 2 * P, 1.028F, -6 * P + width, -23 * P + i * 2 * P, 1.03F, 0xFF9FF4FF, plein);
        }
        float[] hand = {(float) Math.sin(t * 0.07) * 0.9F + (float) Math.sin(t * 0.31) * 0.15F, 1.2F - 2.4F * sub.getEnergy() / Submarine.MAX_ENERGY};
        float[] places = {-0.45F, 0.45F};
        for (int i = 0; i < 2; i++) {
            box(vc, pose, places[i] - 0.13F, -0.87F, 1.04F, places[i] + 0.13F, -0.61F, 1.045F, FRAME_LIGHT, light);
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
            pose.mulPose(Axis.ZP.rotation(hand[i]));
            box(vc, pose, -0.007F, 0.0F, -0.004F, 0.007F, 0.095F, 0.0F, i == 0 ? 0xFF2A2A2A : 0xFFD8402C, plein);
            pose.popPose();
            box(vc, pose, places[i] - 0.015F, -0.755F, 1.026F, places[i] + 0.015F, -0.725F, 1.03F, 0xFF555555, plein);
        }
        int[] leds = {0xFF4FE07A, 0xFFE6B53A, 0xFFE0482F, 0xFF4FA8F0};
        for (int i = 0; i < 4; i++) {
            float x = -0.6F + i * 0.06F;
            boolean on = i != 2 || (sub.tickCount / 10) % 2 == 0;
            box(vc, pose, x - 0.02F, -0.9F, 1.04F, x + 0.02F, -0.86F, 1.045F, on ? leds[i] : 0xFF30343A, plein);
        }
        boolean targeted = seeButton(mc, sub);
        plates(vc, pose, -36 * P, -39 * P, 0.0F, -22 * P, -24 * P, 20 * P, 0xFF2F353D, light);
        box(vc, pose, -37 * P, -24 * P, -P, -21 * P, -23 * P, 21 * P, FRAME_LIGHT, light);
        float top = -23 * P;
        box(vc, pose, -32 * P, top + 0.002F, 6 * P, -25 * P, top + 0.01F, 11 * P, 0xFFE6B53A, light);
        box(vc, pose, -31 * P, top + 0.012F, 7 * P, -26 * P, top + 0.025F, 10 * P, targeted ? 0xFFFFE27A : 0xFFC8962A, targeted ? plein : light);
        box(vc, pose, -30 * P, top + 0.027F, 8 * P, -27 * P, top + 0.032F, 9 * P, 0xFF1A1A1A, plein);
        box(vc, pose, -29 * P, top + 0.027F, 7 * P, -28 * P, top + 0.032F, 8 * P, 0xFF1A1A1A, plein);
        pose.pushPose();
        pose.translate(0.0F, -1.22F, 0.55F);
        box(vc, pose, -0.09F, 0.0F, -0.09F, 0.09F, 0.03F, 0.09F, METAL_DARK, light);
        box(vc, pose, -0.05F, 0.03F, -0.05F, 0.05F, 0.06F, 0.05F, 0xFF1E2226, light);
        pose.mulPose(Axis.XP.rotationDegrees(leverY * 22.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(-leverX * 22.0F));
        box(vc, pose, -0.02F, 0.06F, -0.02F, 0.02F, 0.36F, 0.02F, 0xFF8C949C, light);
        box(vc, pose, -0.04F, 0.36F, -0.04F, 0.04F, 0.52F, 0.04F, 0xFF22262B, light);
        box(vc, pose, -0.03F, 0.52F, -0.03F, 0.03F, 0.545F, 0.03F, 0xFFB02226, light);
        box(vc, pose, -0.015F, 0.545F, -0.015F, 0.015F, 0.555F, 0.015F, 0xFFE8524E, plein);
        pose.popPose();
        pose.popPose();
        buffers.endBatch(TYPE);

        org.joml.Vector3f handle = new org.joml.Vector3f(0.0F, 0.5F, 0.0F)
                .rotateZ((float) Math.toRadians(-leverX * 22.0F)).rotateX((float) Math.toRadians(leverY * 22.0F)).add(0.0F, -1.22F, 0.55F);
        RenderType skin = RenderType.entityTranslucent(mc.player.getSkin().texture());
        VertexConsumer arm = buffers.getBuffer(skin);
        int lightSkin = LightTexture.pack(13, 13);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
        arm(arm, pose, new org.joml.Vector3f(-0.4F, -0.65F, -0.1F), new org.joml.Vector3f(handle).add(-0.13F, -0.03F, -0.02F), 40, 16, lightSkin);
        arm(arm, pose, new org.joml.Vector3f(0.4F, -0.65F, -0.1F), new org.joml.Vector3f(handle).add(0.13F, -0.03F, -0.02F), 32, 48, lightSkin);
        pose.popPose();
        buffers.endBatch(skin);
    }

    public static boolean seeButton(Minecraft mc, Submarine sub) {
        if (!mc.options.getCameraType().isFirstPerson()) return false;
        org.joml.Vector3f view = mc.player.getViewVector(1.0F).toVector3f().rotateY((float) Math.toRadians(sub.getYRot()));
        if (view.y > -0.05F) return false;
        float t = -23 * P / view.y, x = view.x * t, z = view.z * t;
        return x > -32 * P && x < -25 * P && z > 6 * P && z < 11 * P;
    }

    @SubscribeEvent
    public static void gaze(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        boolean ctrl = mc.player != null && mc.player.getVehicle() instanceof Submarine && mc.options.keySprint.isDown();
        if (Submarine.freeGaze && !ctrl) Submarine.rattrapage = true;
        if (ctrl || mc.player == null || !(mc.player.getVehicle() instanceof Submarine)) Submarine.rattrapage = false;
        Submarine.freeGaze = ctrl;
    }

    @SubscribeEvent
    public static void onClick(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (!event.isUseItem() || mc.player == null || !(mc.player.getVehicle() instanceof Submarine sub) || !seeButton(mc, sub)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(net.ussm.the_shell.network.CabinButtonPayload.INSTANCE);
    }

    private static void arm(VertexConsumer vc, PoseStack pose, org.joml.Vector3f shoulder, org.joml.Vector3f hand, int u, int v, int light) {
        org.joml.Vector3f dir = new org.joml.Vector3f(hand).sub(shoulder);
        float l = dir.length();
        pose.pushPose();
        pose.translate(shoulder.x, shoulder.y, shoulder.z);
        pose.mulPose(new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0.0F, 0.0F, 1.0F), dir.normalize()));
        PoseStack.Pose p = pose.last();
        float px = 1.0F / 16.0F, w = 2 * px;
        float elbow = l - 12 * px;
        float[][] sides = {{-w, -w, -w, w}, {-w, w, w, w}, {w, w, w, -w}, {w, -w, -w, -w}};
        for (int i = 0; i < 4; i++) {
            float[] c = sides[i];
            float u0 = (u + i * 4) / 64.0F, u1 = (u + i * 4 + 4) / 64.0F, v0 = (v + 4) / 64.0F, v1 = (v + 16) / 64.0F, vh = (v + 5) / 64.0F;
            float nx = (c[0] + c[2]) / 2, ny = (c[1] + c[3]) / 2;
            summit(vc, p, c[0], c[1], -0.6F, u0, v0, light, nx, ny, 0);
            summit(vc, p, c[0], c[1], elbow, u0, vh, light, nx, ny, 0);
            summit(vc, p, c[2], c[3], elbow, u1, vh, light, nx, ny, 0);
            summit(vc, p, c[2], c[3], -0.6F, u1, v0, light, nx, ny, 0);
            summit(vc, p, c[0], c[1], elbow, u0, v0, light, nx, ny, 0);
            summit(vc, p, c[0], c[1], l, u0, v1, light, nx, ny, 0);
            summit(vc, p, c[2], c[3], l, u1, v1, light, nx, ny, 0);
            summit(vc, p, c[2], c[3], elbow, u1, v0, light, nx, ny, 0);
        }
        float bu0 = (u + 8) / 64.0F, bu1 = (u + 12) / 64.0F, bv0 = v / 64.0F, bv1 = (v + 4) / 64.0F;
        summit(vc, p, -w, -w, l, bu0, bv0, light, 0, 0, 1);
        summit(vc, p, -w, w, l, bu0, bv1, light, 0, 0, 1);
        summit(vc, p, w, w, l, bu1, bv1, light, 0, 0, 1);
        summit(vc, p, w, -w, l, bu1, bv0, light, 0, 0, 1);
        pose.popPose();
    }

    private static void summit(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float v, int light, float nx, float ny, float nz) {
        vc.addVertex(p, x, y, z).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(p, nx, ny, nz);
    }

    private static void plates(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        float step = 6 * P;
        int nx = Math.max(1, Math.round((x1 - x0) / step)), ny = Math.max(1, Math.round((y1 - y0) / step)), nz = Math.max(1, Math.round((z1 - z0) / step));
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    int h = (i * 73856093) ^ (j * 19349663) ^ (k * 83492791) ^ argb;
                    float f = 0.9F + ((h >>> 8) & 255) / 255.0F * 0.18F;
                    box(vc, pose, x0 + (x1 - x0) * i / nx, y0 + (y1 - y0) * j / ny, z0 + (z1 - z0) * k / nz,
                            x0 + (x1 - x0) * (i + 1) / nx, y0 + (y1 - y0) * (j + 1) / ny, z0 + (z1 - z0) * (k + 1) / nz, tint(argb, f), light);
                }
            }
        }
    }

    private static final float P = 1.0F / 32.0F;

    private static float[] grid(float a0, float a1) {
        float b0 = Math.round(a0 / P) * P, b1 = Math.round(a1 / P) * P;
        if (b1 - b0 < P) {
            b0 = Math.round(((a0 + a1) / 2 - P / 2) / P) * P;
            b1 = b0 + P;
        }
        return new float[]{b0, b1};
    }

    private static void box(VertexConsumer vc, PoseStack pose, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        int thin = dx <= dy && dx <= dz ? 0 : dy <= dz ? 1 : 2;
        boolean thick = Math.min(dx, Math.min(dy, dz)) >= P * 0.75F;
        float[] g;
        if (thick || thin != 0) { g = grid(x0, x1); x0 = g[0]; x1 = g[1]; }
        if (thick || thin != 1) { g = grid(y0, y1); y0 = g[0]; y1 = g[1]; }
        if (thick || thin != 2) { g = grid(z0, z1); z0 = g[0]; z1 = g[1]; }
        PoseStack.Pose p = pose.last();
        quad(vc, p, tint(argb, 0.55F), light, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(vc, p, tint(argb, 1.0F), light, 0, 1, 0, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        quad(vc, p, tint(argb, 0.8F), light, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(vc, p, tint(argb, 0.8F), light, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        quad(vc, p, tint(argb, 0.68F), light, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        quad(vc, p, tint(argb, 0.68F), light, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static int tint(int argb, float f) {
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
