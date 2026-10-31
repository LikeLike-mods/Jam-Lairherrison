package net.ussm.the_shell.client.renderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.ussm.the_shell.client.model.TurningEntityGeoModel;
import net.ussm.the_shell.entites.PurussaurusEntity;
import net.ussm.the_shell.entites.animations.SwimmingTurnProfiles;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class PurussaurusRenderer extends GeoEntityRenderer<PurussaurusEntity> {
    public PurussaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new TurningEntityGeoModel<>("purussaurus", SwimmingTurnProfiles.PURUSSAURUS));
        shadowRadius = 1.6F;
    }
}
