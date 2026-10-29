package net.ussm.the_shell.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static FoodProperties NECTAR = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3f)
            .alwaysEdible()
            .effect(new MobEffectInstance(MobEffects.JUMP, 600, 1), 1.0f)
            .build();

    public static FoodProperties COCONUT_HALF = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.5f)
            .usingConvertsTo(ModItems.COCONUT_SHELL.get())
            .build();

    public static FoodProperties REDSTONE_PEPPER = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.1f)
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 10), 1.0f)
            .fast()
            .build();

    public  static FoodProperties RAW_CRAB_MEAT = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3F)
            .effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1)
            .build();

    public static FoodProperties COOKED_CRAB_MEAT = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6F)
            .build();
}
