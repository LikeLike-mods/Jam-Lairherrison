package net.ussm.the_shell.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.init.ModBlocks;

// une fois mangee il reste la peau (qu'on peut poser par terre pour faire glisser les autres)
public class BananaItem extends Item {
    public BananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack reste = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack peau = new ItemStack(ModBlocks.BANANA_PEEL.get());
            if (reste.isEmpty()) return peau;
            if (!player.getInventory().add(peau)) player.drop(peau, false);
        }
        return reste;
    }
}
