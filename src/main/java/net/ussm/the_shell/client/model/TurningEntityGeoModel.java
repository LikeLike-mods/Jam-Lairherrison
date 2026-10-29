package net.ussm.the_shell.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.animation.AdditiveBonePose;
import net.ussm.the_shell.entites.animations.TurnBend;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import net.ussm.the_shell.entites.animations.TurnBendingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TurningEntityGeoModel<T extends LivingEntity & GeoAnimatable & TurnBendingEntity> extends DefaultedEntityGeoModel<T> {
    private final TurnBendProfile profile;
    private final AdditiveBonePose turnPose = new AdditiveBonePose();

    public TurningEntityGeoModel(String name, TurnBendProfile profile) {
        super(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, name), false);
        this.profile = profile;
    }

    @Override
    public void applyMolangQueries(AnimationState<T> state, double animationTime) {
        // This hook runs before GeckoLib animates; same-frame glow/re-render passes keep their pose.
        turnPose.clear();
        super.applyMolangQueries(state, animationTime);
    }

    @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> state) {
        super.setCustomAnimations(entity, instanceId, state);
        if (!entity.isTurnBendingAllowed()) return;
        float partialTick = state.getPartialTick();
        TurnBend.apply(profile, entity.getTurnBend(partialTick), entity.getViewXRot(partialTick),
                (name, x, y, z) -> turnPose.add(getAnimationProcessor()::getBone, name, x, y, z));
    }
}
