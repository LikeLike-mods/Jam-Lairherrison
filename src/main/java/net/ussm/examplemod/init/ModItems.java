package net.ussm.examplemod.init;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MOD_ID);

    public static final DeferredItem<Item> COCONUT = ITEMS.register("coconut",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(0.4f)
                    .build())));


    // Nectar de népenthès : se boit comme du miel, donne un super saut
    public static final DeferredItem<Item> NECTAR = ITEMS.register("nectar",
            () -> new HoneyBottleItem(new Item.Properties()
                    .craftRemainder(Items.GLASS_BOTTLE)
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(3)
                            .saturationModifier(0.3f)
                            .alwaysEdible()
                            .effect(new MobEffectInstance(MobEffects.JUMP, 600, 1), 1.0f)
                            .build())));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
