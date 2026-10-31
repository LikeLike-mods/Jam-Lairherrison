package net.ussm.the_shell.entites;

import net.minecraft.sounds.SoundEvent;
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

public class Firefly extends FlyingMobs {
    public Firefly(EntityType<? extends Firefly> type, Level level) {
        super(type, level, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.FLYING_SPEED, 0.35)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        goalSelector.addGoal(2, new InsectWanderGoal(this, 0.8, 6, 0.8F, 3.2F, 25));
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return level().isNight() ? super.getAmbientSound() : null;
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
