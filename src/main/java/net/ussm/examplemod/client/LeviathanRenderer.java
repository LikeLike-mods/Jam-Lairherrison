package net.ussm.examplemod.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.ussm.examplemod.client.model.LeviathanModel;
import net.ussm.examplemod.entity.LeviathanEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class LeviathanRenderer extends GeoEntityRenderer<LeviathanEntity> {
    public LeviathanRenderer(EntityRendererProvider.Context context) {
        super(context, new LeviathanModel());
        withScale(LeviathanEntity.SIZE_MULTIPLIER);
        shadowRadius = 1.3F * LeviathanEntity.SIZE_MULTIPLIER;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
