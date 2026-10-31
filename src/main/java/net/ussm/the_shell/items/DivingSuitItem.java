package net.ussm.the_shell.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModComponents;

public class DivingSuitItem extends ArmorItem {
    public DivingSuitItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public static boolean isReinforced(ItemStack stack) {
        return stack.getItem() instanceof DivingSuitItem && stack.getOrDefault(ModComponents.REINFORCED, false);
    }

    @Override
    public Component getName(ItemStack stack) {
        return isReinforced(stack) ? Component.translatable(getDescriptionId(stack) + ".reinforced") : super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (isReinforced(stack)) {
            tooltip.add(Component.translatable("tooltip.the_shell.diving_suit.reinforced").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (!isReinforced(stack)) return null;
        return ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "textures/models/armor/diving_reinforced_layer_" + (innerModel ? 2 : 1) + ".png");
    }
}
