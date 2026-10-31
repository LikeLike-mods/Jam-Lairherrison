package net.ussm.the_shell.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModTags;
import net.ussm.the_shell.items.DivingSuitItem;

@EventBusSubscriber(modid = TheShell.MOD_ID)
public class ReinforcedSuitEvents {
    private static final float REDUCTION_PER_PIECE = 0.05F;

    @SubscribeEvent
    public static void armor(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!DivingSuitItem.isReinforced(stack) || !(stack.getItem() instanceof ArmorItem armor)) return;
        ArmorItem.Type type = armor.getType();
        int value = switch (type) {
            case HELMET, BOOTS -> 3;
            case CHESTPLATE -> 8;
            case LEGGINGS, BODY -> 6;
        };
        event.replaceModifier(Attributes.ARMOR,
                new AttributeModifier(ResourceLocation.withDefaultNamespace("armor." + type.getName()), value, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(type.getSlot()));
    }

    @SubscribeEvent
    public static void seaCreatureDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || !attacker.getType().is(ModTags.SEA_CREATURES)) return;
        LivingEntity target = event.getEntity();
        int pieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (DivingSuitItem.isReinforced(target.getItemBySlot(slot))) pieces++;
        }
        if (pieces > 0) event.setAmount(event.getAmount() * (1.0F - REDUCTION_PER_PIECE * pieces));
    }
}
