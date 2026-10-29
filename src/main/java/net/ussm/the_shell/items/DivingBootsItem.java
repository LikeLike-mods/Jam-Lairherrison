package net.ussm.the_shell.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.ussm.the_shell.init.ModComponents;

import java.util.List;

public class DivingBootsItem extends ArmorItem {
    public DivingBootsItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.BOOTS, properties);
    }

    public static boolean isWeighted(ItemStack stack) {
        return stack.getItem() instanceof DivingBootsItem && stack.getOrDefault(ModComponents.WEIGHTED, false);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack boots, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !other.is(Items.IRON_INGOT) || isWeighted(boots)) return false;
        boots.set(ModComponents.WEIGHTED, true);
        other.shrink(1);
        player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 0.9F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(isWeighted(stack) ? "tooltip.the_shell.diving_boots.weighted" : "tooltip.the_shell.diving_boots.light")
                .withStyle(ChatFormatting.GRAY));
    }
}
