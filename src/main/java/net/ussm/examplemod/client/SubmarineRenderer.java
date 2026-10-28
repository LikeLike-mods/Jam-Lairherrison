package net.ussm.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.entity.Submarine;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SubmarineRenderer extends GeoEntityRenderer<Submarine> {

    public SubmarineRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "submarine")));
        addRenderLayer(new EyesGlowLayer<>(this));
        this.shadowRadius = 1.0F;
    }

    // c'est pas un mob, geckolib le tourne pas tout seul
    @Override
    protected void applyRotations(Submarine submarine, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTick, submarine.yRotO, submarine.getYRot())));
    }

    // translucide pour la vitre du cockpit
    @Override
    public RenderType getRenderType(Submarine submarine, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
