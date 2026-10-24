package net.ussm.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

// Toucan : vole dans la jungle, avance par petits sauts quand il est au sol.
public class Toucan extends AtollFlyer {
    public Toucan(EntityType<? extends Toucan> type, Level level) {
        super(type, level, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 4, state -> {
            if (this.isFlying()) return state.setAndContinue(DefaultAnimations.FLY);
            return state.setAndContinue(state.isMoving() ? AtollAnimations.HOP : DefaultAnimations.IDLE);
        }));
    }
}
