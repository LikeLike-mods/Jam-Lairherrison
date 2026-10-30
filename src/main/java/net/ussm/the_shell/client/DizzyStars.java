package net.ussm.the_shell.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class DizzyStars {
    private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/misc/dizzy_star.png");

    @SubscribeEvent
    public static void drawScreen(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.getData(ModAttachments.SLIPPED) || !mc.options.getCameraType().isFirstPerson() || mc.options.hideGui) return;
        GuiGraphics g = event.getGuiGraphics();
        float t = (mc.player.tickCount + event.getPartialTick().getGameTimeDeltaPartialTick(false)) * 0.12F;
        int cx = g.guiWidth() / 2, cy = g.guiHeight() / 4;
        RenderSystem.enableBlend();
        for (int i = 0; i < 5; i++) {
            double a = t + i * Math.PI * 2 / 5;
            boolean ahead = Math.sin(a) > 0;
            int size = ahead ? 32 : 16;
            int x = cx + (int) (Math.cos(a) * g.guiWidth() * 0.13) - size / 2;
            int y = cy + (int) (Math.sin(a) * g.guiHeight() * 0.05) - size / 2;
            g.blit(STAR_TEXTURE, x, y, size, size, 0, 0, 16, 16, 16, 16);
        }
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void ascend(net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre event) {
        event.getPoseStack().pushPose();
        if (event.getEntity().getData(ModAttachments.SLIPPED)) event.getPoseStack().translate(0.0F, 0.28F, 0.0F);
    }

    @SubscribeEvent
    public static void descend(net.neoforged.neoforge.client.event.RenderPlayerEvent.Post event) {
        event.getPoseStack().popPose();
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model model : event.getSkins()) {
            if (event.getSkin(model) instanceof PlayerRenderer renderer) renderer.addLayer(new Couronne(renderer));
        }
    }

    private static class Couronne extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        Couronne(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!player.getData(ModAttachments.SLIPPED)) return;
            VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(STAR_TEXTURE));
            pose.pushPose();
            getParentModel().head.translateAndRotate(pose);
            pose.translate(0.0F, -0.25F, 0.0F);
            org.joml.Vector3f head = pose.last().pose().transformPosition(new org.joml.Vector3f());
            pose.popPose();
            PoseStack world = new PoseStack();
            world.translate(head.x, head.y, head.z);
            for (int i = 0; i < 5; i++) {
                double a = ageInTicks * 0.2 + i * Math.PI * 2 / 5;
                world.pushPose();
                world.translate(Math.cos(a) * 0.42, 0.4 + Math.sin(ageInTicks * 0.3 + i) * 0.03, Math.sin(a) * 0.42);
                PoseStack.Pose p = world.last();
                float s = 0.11F;
                square(vc, p, -s, s, 0, s, -s, 0);
                square(vc, p, 0, s, -s, 0, -s, s);
                world.popPose();
            }
        }

        private static void square(VertexConsumer vc, PoseStack.Pose p, float x0, float y0, float z0, float x1, float y1, float z1) {
            summit(vc, p, x0, y1, z0, 0, 1);
            summit(vc, p, x1, y1, z1, 1, 1);
            summit(vc, p, x1, y0, z1, 1, 0);
            summit(vc, p, x0, y0, z0, 0, 0);
        }

        private static void summit(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float v) {
            vc.addVertex(p, x, y, z).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 1, 0);
        }
    }
}
