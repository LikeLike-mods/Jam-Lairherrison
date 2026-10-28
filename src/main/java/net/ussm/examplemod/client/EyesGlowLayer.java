package net.ussm.examplemod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

// Ce qui brille (texture _glowmask) est rendu comme les yeux des endermans : en additif et en pleine lumière, donc bien
// visible dans le noir des abysses.
public class EyesGlowLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    public EyesGlowLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ResourceLocation texture = getRenderer().getTextureLocation(animatable);
        ResourceLocation glow = ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), texture.getPath().replace(".png", "_glowmask.png"));
        RenderType eyes = RenderType.eyes(glow);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyes, bufferSource.getBuffer(eyes), partialTick,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
