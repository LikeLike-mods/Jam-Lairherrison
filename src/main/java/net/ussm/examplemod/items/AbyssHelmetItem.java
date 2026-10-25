package net.ussm.examplemod.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Crâne vivant fusionné avec un casque (table de forgeron) : il protège comme le casque, garde ses enchantements
// (le forgeron recopie ceux du casque) et éclaire autour du joueur comme le crâne vivant.
public class AbyssHelmetItem extends Item implements Equipable {
    private final int enchantmentValue;

    public AbyssHelmetItem(Properties properties, int enchantmentValue) {
        super(properties);
        this.enchantmentValue = enchantmentValue;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }
}
