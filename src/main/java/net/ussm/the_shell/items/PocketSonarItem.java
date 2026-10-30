package net.ussm.the_shell.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PocketSonarItem extends Item {

    public PocketSonarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            net.ussm.the_shell.client.PocketSonarClient.ping(player);
        } else {
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24.0), e -> e != player)) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0, false, false));
            }
            level.playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.6F);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        player.getCooldowns().addCooldown(this, 100);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
