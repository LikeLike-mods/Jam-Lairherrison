package net.ussm.the_shell.client.renderers;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.entites.Crab;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CrabRenderer extends GeoEntityRenderer<Crab> {
    public CrabRenderer(EntityRendererProvider.Context context) {
        super(context, new CrabModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public RenderType getRenderType(Crab animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(texture);
    }

    private static class CrabModel extends DefaultedEntityGeoModel<Crab> {
        private final ResourceLocation babyModel;
        private final ResourceLocation babyTexture;

        CrabModel() {
            super(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "crab"), false);
            ResourceLocation baby = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "crab_baby");
            this.babyModel = buildFormattedModelPath(baby);
            this.babyTexture = buildFormattedTexturePath(baby);
        }

        @Override
        public ResourceLocation getModelResource(Crab animatable) {
            return animatable.isBaby() ? babyModel : super.getModelResource(animatable);
        }

        @Override
        public ResourceLocation getTextureResource(Crab animatable) {
            return animatable.isBaby() ? babyTexture : super.getTextureResource(animatable);
        }
    }
}
