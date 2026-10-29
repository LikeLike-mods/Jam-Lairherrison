package net.ussm.theshell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.ussm.theshell.blocks.MessageBottleBlock;
import net.ussm.theshell.blocks.MessageBottleBlockEntity;

public class MessageBottleRenderer implements BlockEntityRenderer<MessageBottleBlockEntity> {
    private static final float SCALE = 0.25F;
    private static final float[][] LYING = {{-5.0F, 0.3F, 0.2F, 8}, {-2.6F, 0.3F, -0.2F, -10}, {-3.8F, 1.1F, 0.0F, 5}, {-1.4F, 1.1F, 0.0F, -6}};
    private static final float[][] STANDING = {{0.1F, 0.3F, -0.1F, 5}, {-0.1F, 1.4F, 0.1F, 95}, {0.1F, 2.5F, 0.1F, 183}, {-0.1F, 3.6F, -0.1F, 268}};
    private final ItemRenderer itemRenderer;

    public MessageBottleRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(MessageBottleBlockEntity bottle, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean standing = bottle.getBlockState().getValue(MessageBottleBlock.STANDING);
        float[][] slots = standing ? STANDING : LYING;
        int shown = 0;
        for (ItemStack stack : bottle.getItems()) {
            if (stack.isEmpty()) continue;
            float[] slot = slots[shown];
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F - bottle.getBlockState().getValue(MessageBottleBlock.ROTATION) * 22.5F));
            poseStack.translate(slot[0] / 16.0F, slot[1] / 16.0F, slot[2] / 16.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(slot[3]));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(SCALE, SCALE, SCALE);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, bottle.getLevel(), (int) bottle.getBlockPos().asLong() + shown);
            poseStack.popPose();
            shown++;
        }
    }
}
