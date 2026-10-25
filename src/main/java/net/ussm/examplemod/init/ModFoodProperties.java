package net.ussm.examplemod.init;

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
}
