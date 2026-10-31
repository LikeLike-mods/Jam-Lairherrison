package net.ussm.the_shell.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.ussm.the_shell.client.model.TitanoboaModel;
import net.ussm.the_shell.entites.TitanoboaEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class TitanoboaRenderer extends GeoEntityRenderer<TitanoboaEntity> {
    public TitanoboaRenderer(EntityRendererProvider.Context context) {
        super(context, new TitanoboaModel());
        shadowRadius = 0.8F;
    }

    @Override
    protected void applyRotations(TitanoboaEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                  float partialTick, float nativeScale) {
        double[] j = animatable.joints(partialTick);
        poseStack.translate(j[0] - Mth.lerp(partialTick, animatable.xOld, animatable.getX()),
                j[1] - Mth.lerp(partialTick, animatable.yOld, animatable.getY()),
                j[2] - Mth.lerp(partialTick, animatable.zOld, animatable.getZ()));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - animatable.visualYaw(partialTick)));
        if (animatable.deathTime > 0) {
            float roll = Math.min(1.0F, Mth.sqrt((animatable.deathTime + partialTick - 1.0F) / 20.0F * 1.6F));
            poseStack.translate(0.0F, 0.4F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll * 180.0F));
            poseStack.translate(0.0F, -0.4F, 0.0F);
        }
    }
}
