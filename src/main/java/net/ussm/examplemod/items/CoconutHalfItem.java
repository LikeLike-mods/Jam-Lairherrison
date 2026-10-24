package net.ussm.examplemod.items;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

// Demi-noix de coco : on boit le lait et on mange la chair. Le lait soigne le poison (grenouille, méduse) ;
// il reste la coque vide (usingConvertsTo dans ModItems).
public class CoconutHalfItem extends Item {
    public CoconutHalfItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.removeEffect(MobEffects.POISON);
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
