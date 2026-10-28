package net.ussm.examplemod.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.ussm.examplemod.event.AtollTravel;

// Conque : on souffle dedans comme dans une corne de chèvre (un clic suffit) et elle fait sortir de l'atoll (ou y ramène).
// 30 secondes de recharge pour ne pas en faire une téléportation à volonté sur le SMP.
public class ConchItem extends Item {
    private static final int BLOW_TICKS = 40;   // durée de l'animation seulement
    private static final int COOLDOWN = 600;

    public ConchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        player.getCooldowns().addCooldown(this, COOLDOWN);
        // le son est joué par le serveur une fois arrivé, sinon il reste dans l'ancienne dimension et on ne l'entend pas
        if (player instanceof ServerPlayer serverPlayer) {
            AtollTravel.blowConch(serverPlayer);
            serverPlayer.level().playSound(null, serverPlayer, SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), SoundSource.PLAYERS, 1.0F, 1.3F);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return BLOW_TICKS;
    }
}
