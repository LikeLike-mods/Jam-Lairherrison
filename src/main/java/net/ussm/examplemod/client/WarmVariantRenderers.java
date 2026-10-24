package net.ussm.examplemod.client;

import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModAttachments;

public class WarmVariantRenderers {
    private static final ResourceLocation WARM_COW = texture("warm_cow");
    private static final ResourceLocation WARM_PIG = texture("warm_pig");
    private static final ResourceLocation WARM_CHICKEN = texture("warm_chicken");

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "textures/entity/" + name + ".png");
    }

    public static class WarmCowRenderer extends CowRenderer {
        public WarmCowRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(Cow entity) {
            return entity.getData(ModAttachments.WARM_VARIANT) ? WARM_COW : super.getTextureLocation(entity);
        }
    }

    public static class WarmPigRenderer extends PigRenderer {
        public WarmPigRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(Pig entity) {
            return entity.getData(ModAttachments.WARM_VARIANT) ? WARM_PIG : super.getTextureLocation(entity);
        }
    }

    public static class WarmChickenRenderer extends ChickenRenderer {
        public WarmChickenRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(Chicken entity) {
            return entity.getData(ModAttachments.WARM_VARIANT) ? WARM_CHICKEN : super.getTextureLocation(entity);
        }
    }
}
