package net.ussm.the_shell.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.events.DeepSeaEvents;
import net.ussm.the_shell.init.ModAttachments;

public class OxygenBottleItem extends Item {
    private static final int CHARGE = ModAttachments.MAX_OXYGEN / 4;

    public OxygenBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() || player.getData(ModAttachments.OXYGEN) > ModAttachments.MAX_OXYGEN - CHARGE / 4) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            DeepSeaEvents.giveOxygen(player, CHARGE);
            stack.setDamageValue(stack.getDamageValue() + 1);
            level.playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int charges = stack.getMaxDamage() - stack.getDamageValue();
        tooltip.add(charges > 0 ? Component.translatable("item.the_shell.oxygen_bottle.charges", charges).withStyle(ChatFormatting.AQUA)
                : Component.translatable("item.the_shell.oxygen_bottle.empty").withStyle(ChatFormatting.GRAY));
    }
}
