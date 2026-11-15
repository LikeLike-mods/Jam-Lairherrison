package net.ussm.the_shell.entites;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entites.animations.Animations;
import net.ussm.the_shell.entites.init.FlyingMobs;
import net.ussm.the_shell.events.EncyclopedieHelper;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

public class Toucan extends FlyingMobs {
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
            return state.setAndContinue(state.isMoving() ? Animations.HOP : DefaultAnimations.IDLE);
        }));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (EncyclopedieHelper.holdsBook(player, hand)) {
            if (!level().isClientSide) {
                EncyclopedieHelper.unlock(player, "encyclopedie/find_toucan");
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
