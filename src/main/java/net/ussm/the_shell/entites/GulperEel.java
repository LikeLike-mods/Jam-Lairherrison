package net.ussm.the_shell.entites;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.entites.init.AbyssalPredator;
import net.ussm.the_shell.events.EncyclopedieHelper;
import software.bernie.geckolib.constant.DefaultAnimations;

public class GulperEel extends AbyssalPredator {
    public GulperEel(EntityType<? extends GulperEel> type, Level level) {
        super(type, level, 12.0, DefaultAnimations.ATTACK_BITE, 0.8, 40);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 20.0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (EncyclopedieHelper.holdsBook(player, hand)) {
            if (!level().isClientSide) {
                EncyclopedieHelper.unlock(player, "encyclopedie/find_guper_eel");
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
