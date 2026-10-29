package net.ussm.the_shell.client.renderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.ussm.the_shell.client.model.LeviathanModel;
import net.ussm.the_shell.entites.LeviathanEntity;
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
