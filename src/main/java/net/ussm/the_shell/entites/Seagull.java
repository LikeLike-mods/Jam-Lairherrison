package net.ussm.the_shell.entites;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entites.init.FlyingMobs;
import net.ussm.the_shell.events.EncyclopedieHelper;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;

public class Seagull extends FlyingMobs {
    public Seagull(EntityType<? extends Seagull> type, Level level) {
        super(type, level, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 4, state -> {
            if (this.isFlying()) {
                return state.setAndContinue(DefaultAnimations.FLY);
            }
            return state.setAndContinue(state.isMoving() ? DefaultAnimations.WALK : DefaultAnimations.IDLE);
        }));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (EncyclopedieHelper.holdsBook(player, hand)) {
            if (!level().isClientSide) {
                EncyclopedieHelper.unlock(player, "encyclopedie/find_seagull");
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
