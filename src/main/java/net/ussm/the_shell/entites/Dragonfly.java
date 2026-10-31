package net.ussm.the_shell.entites;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entites.ai.InsectWanderGoal;
import net.ussm.the_shell.entites.init.FlyingMobs;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

public class Dragonfly extends FlyingMobs {
    public Dragonfly(EntityType<? extends Dragonfly> type, Level level) {
        super(type, level, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.FLYING_SPEED, 0.9)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(2, new InsectWanderGoal(this, 1.0, 10, 0.6F, 2.6F, 15));
    }

    @Override
    public int getAmbientSoundInterval() {
        return 220;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 2, state ->
                state.setAndContinue(isFlying() || state.isMoving() ? DefaultAnimations.FLY : DefaultAnimations.IDLE)));
    }
}
