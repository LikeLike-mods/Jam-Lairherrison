package net.ussm.the_shell.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.ussm.the_shell.init.ModItems;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.events.AtollTravel;

public class ConchItem extends Item {
    private static final int BLOW_TICKS = 40;
    private static final int COOLDOWN = 600;
    // each blow (there or back) uses one charge; a kyber crystal gives 5 back. The item keeps one hidden point so it never breaks.
    public static final int MAX_USES = 20;
    private static final int CRYSTAL_USES = 5;

    public static int usesLeft(ItemStack stack) {
        return Math.max(0, MAX_USES - stack.getDamageValue());
    }

    public ConchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (usesLeft(stack) <= 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.the_shell.conch.empty"), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        player.getCooldowns().addCooldown(this, COOLDOWN);
        if (player instanceof ServerPlayer serverPlayer) {
            stack.setDamageValue(Math.min(MAX_USES, stack.getDamageValue() + 1));
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

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    // right-click in the inventory with a kyber crystal on the cursor
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.is(ModItems.KYBER_CRYSTAL.get()) || stack.getDamageValue() == 0) return false;
        stack.setDamageValue(Math.max(0, stack.getDamageValue() - CRYSTAL_USES));
        other.shrink(1);
        player.playSound(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), 0.4F, 1.6F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.the_shell.conch.uses", usesLeft(stack), MAX_USES).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.the_shell.conch.recharge").withStyle(ChatFormatting.DARK_AQUA));
    }
}
