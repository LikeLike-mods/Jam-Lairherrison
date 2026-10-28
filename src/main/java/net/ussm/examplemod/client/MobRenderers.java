package net.ussm.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.ussm.examplemod.ExampleMod;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MobRenderers<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {
    public enum SwimPose { NONE, PITCH, LEAN }

    private final boolean translucent;
    private final SwimPose swimPose;

    public MobRenderers(EntityRendererProvider.Context context, String name, float shadowRadius,
                            boolean turnsHead, boolean glows, boolean translucent) {
        this(context, name, shadowRadius, turnsHead, glows, translucent, SwimPose.NONE);
    }

    public MobRenderers(EntityRendererProvider.Context context, String name, float shadowRadius,
                            boolean turnsHead, boolean glows, boolean translucent, SwimPose swimPose) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, name), turnsHead));
        this.shadowRadius = shadowRadius;
        this.translucent = translucent;
        this.swimPose = swimPose;
        if (glows) {
            this.addRenderLayer(new EyesGlowLayer<>(this));
        }
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return translucent ? RenderType.entityTranslucentCull(texture) : RenderType.entityCutout(texture);
    }

    @Override
    protected void applyRotations(T animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
        if (swimPose == SwimPose.NONE || !animatable.isInWater()) return;
        float angle;
        if (swimPose == SwimPose.PITCH) {
            angle = -Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        } else {
            double vitesse = Math.hypot(animatable.getX() - animatable.xo, animatable.getZ() - animatable.zo);
            angle = -(float) Math.min(70.0, vitesse * 600.0);
        }
        float centre = animatable.getBbHeight() / 2.0F;
        poseStack.translate(0.0F, centre, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(angle));
        poseStack.translate(0.0F, -centre, 0.0F);
    }
}
