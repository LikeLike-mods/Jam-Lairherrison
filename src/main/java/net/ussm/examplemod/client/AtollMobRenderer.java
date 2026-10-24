package net.ussm.examplemod.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.ussm.examplemod.ExampleMod;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

// Rendu commun des mobs de l'atoll : modèle, animations et texture GeckoLib rangés sous le nom du mob
// (geo/entity/<nom>.geo.json, animations/entity/<nom>.animation.json, textures/entity/<nom>.png).
public class AtollMobRenderer<T extends Entity & GeoAnimatable> extends GeoEntityRenderer<T> {
    private final boolean translucent;

    public AtollMobRenderer(EntityRendererProvider.Context context, String name, float shadowRadius,
                            boolean turnsHead, boolean glows, boolean translucent) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, name), turnsHead));
        this.shadowRadius = shadowRadius;
        this.translucent = translucent;
        if (glows) {
            // parties lumineuses (textures/entity/<nom>_glowmask.png) qui brillent dans le noir
            this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        }
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return translucent ? RenderType.entityTranslucent(texture) : super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}
