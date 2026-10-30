package net.ussm.the_shell.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.init.ModBlocks;

public class BananaItem extends Item {
    public BananaItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack rest = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            ItemStack peel = new ItemStack(ModBlocks.BANANA_PEEL.get());
            if (rest.isEmpty()) return peel;
            if (!player.getInventory().add(peel)) player.drop(peel, false);
        }
        return rest;
    }
}
