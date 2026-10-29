package net.ussm.the_shell.client.model;

import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.animation.LeviathanTurnBend;
import net.ussm.the_shell.entites.LeviathanEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public final class LeviathanModel extends GeoModel<LeviathanEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            TheShell.MOD_ID, "geo/entity/leviathan.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TheShell.MOD_ID, "textures/entity/leviathan.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(
            TheShell.MOD_ID, "animations/entity/leviathan.animation.json");

    @Override
    public ResourceLocation getModelResource(LeviathanEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(LeviathanEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(LeviathanEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(LeviathanEntity entity, long instanceId, AnimationState<LeviathanEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        float partialTick = state.getPartialTick();
        LeviathanTurnBend.apply(entity.getTurnBend(partialTick), entity.getViewXRot(partialTick), (name, x, y, z) -> {
            var bone = getAnimationProcessor().getBone(name);
            if (bone != null) {
                bone.setRotX(bone.getRotX() + x);
                bone.setRotY(bone.getRotY() + y);
                bone.setRotZ(bone.getRotZ() + z);
            }
        });
    }
}
