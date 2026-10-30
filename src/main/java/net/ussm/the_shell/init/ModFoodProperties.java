package net.ussm.the_shell.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties FRIED_LEVIATHAN_EGG = new FoodProperties.Builder()
            .nutrition(20)
            .saturationModifier(0.8F)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 600, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0), 1.0F)
            .build();

    public static FoodProperties NECTAR = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.3f)
            .alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 600, 1), 1.0f)
            .build();

    public static FoodProperties COCONUT_HALF = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.5f)
            .usingConvertsTo(ModItems.COCONUT_SHELL.get())
            .build();

    public static FoodProperties BANANA = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3f)
            .build();

    public static FoodProperties BANANA_BREAD = new FoodProperties.Builder()
            .nutrition(7)
            .saturationModifier(0.6f)
            .build();

    public static FoodProperties BANANA_CHIPS = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.4f)
            .fast()
            .build();

    public static FoodProperties DATES = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.5f)
            .fast()
            .build();


    public static FoodProperties DATE_SYRUP = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3f)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300), 1.0f)
            .build();

    public static FoodProperties REDSTONE_PEPPER = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.1f)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 10), 1.0f)
            .fast()
            .build();

    public  static FoodProperties RAW_CRAB_MEAT = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1)
            .build();

    public static FoodProperties COOKED_CRAB_MEAT = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6F)
            .build();
}
