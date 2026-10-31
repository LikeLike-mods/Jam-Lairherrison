package net.ussm.the_shell.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.EyesGlowLayer;
import net.ussm.the_shell.entites.Submarine;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SubmarineRenderer extends GeoEntityRenderer<Submarine> {

    public SubmarineRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "submarine")));
        addRenderLayer(new EyesGlowLayer<>(this));
        this.shadowRadius = 1.0F;
    }

    @Override
    protected void applyRotations(Submarine submarine, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot())));
    }

    @Override
    public void render(Submarine submarine, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType().isFirstPerson() && submarine.hasPassenger(mc.getCameraEntity())) return;
        super.render(submarine, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public void preRender(PoseStack poseStack, Submarine submarine, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        for (int tier = 1; tier <= 3; tier++) {
            boolean hide = submarine.getTier() < tier;
            model.getBone("tier" + tier).ifPresent(bone -> bone.setHidden(hide));
        }
        super.preRender(poseStack, submarine, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    @Override
    public RenderType getRenderType(Submarine submarine, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
