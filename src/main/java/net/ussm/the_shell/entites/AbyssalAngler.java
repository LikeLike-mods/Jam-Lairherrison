package net.ussm.the_shell.entites;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.blocks.abyss.AbyssLightBlock;
import net.ussm.the_shell.entites.init.AbyssalPredator;
import net.ussm.the_shell.events.EncyclopedieHelper;
import software.bernie.geckolib.constant.DefaultAnimations;

public class AbyssalAngler extends AbyssalPredator {
    public AbyssalAngler(EntityType<? extends AbyssalAngler> type, Level level) {
        super(type, level, 7.0, DefaultAnimations.ATTACK_BITE, 0.5, 120);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.tickCount % 4 == 0) {
            AbyssLightBlock.placeAt(this.level(), BlockPos.containing(this.position().add(0.0, 1.3, 0.0)));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (EncyclopedieHelper.holdsBook(player, hand)) {
            if (!level().isClientSide) {
                EncyclopedieHelper.unlock(player, "encyclopedie/find_abyssal_angler");
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}