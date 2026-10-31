package net.ussm.the_shell.items;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ussm.the_shell.init.ModItems;

// the lapis slot of the enchanting table, which also takes kyber crystals
public class KyberSlot extends Slot {
    public KyberSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Items.LAPIS_LAZULI) || stack.is(ModItems.KYBER_CRYSTAL.get());
    }
}
