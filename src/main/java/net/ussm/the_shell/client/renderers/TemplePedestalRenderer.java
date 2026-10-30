package net.ussm.the_shell.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.ussm.the_shell.blocks.TemplePedestalBlockEntity;

public class TemplePedestalRenderer implements BlockEntityRenderer<TemplePedestalBlockEntity> {
    public TemplePedestalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TemplePedestalBlockEntity socle, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (socle.objet().isEmpty() || socle.getLevel() == null) return;
        float t = socle.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        pose.translate(0.5, 1.25 + Math.sin(t * 0.08) * 0.06, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(t * 2.0F));
        pose.scale(0.7F, 0.7F, 0.7F);
        Minecraft.getInstance().getItemRenderer().renderStatic(socle.objet(), ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, pose, buffers, socle.getLevel(), (int) socle.getBlockPos().asLong());
        pose.popPose();
    }
}
