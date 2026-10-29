package net.ussm.theshell.client;

import net.ussm.theshell.TheShell;
import net.ussm.theshell.item.LightsaberItem;
import net.ussm.theshell.client.mask.MaskedItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class LightsaberRenderer extends MaskedItemRenderer<LightsaberItem> {
    public LightsaberRenderer(String modelName) {
        this(modelName, modelName);
    }

    public LightsaberRenderer(String modelName, String textureName) {
        super(new DefaultedItemGeoModel<LightsaberItem>(
                ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, modelName))
                .withAltTexture(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, textureName)), "blade");

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(LightsaberItem animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
