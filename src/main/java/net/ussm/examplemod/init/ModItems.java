package net.ussm.examplemod.init;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
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

    // Bois de palmier : pancartes et bateaux (le type de bateau vient de ModBoats)
    public static final DeferredItem<Item> PALM_SIGN = ITEMS.register("palm_sign",
            () -> new SignItem(new Item.Properties().stacksTo(16), ModBlocks.PALM_SIGN.get(), ModBlocks.PALM_WALL_SIGN.get()));
    public static final DeferredItem<Item> PALM_HANGING_SIGN = ITEMS.register("palm_hanging_sign",
            () -> new HangingSignItem(ModBlocks.PALM_HANGING_SIGN.get(), ModBlocks.PALM_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> PALM_BOAT = ITEMS.register("palm_boat",
            () -> new BoatItem(false, ModBoats.PALM.getValue(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PALM_CHEST_BOAT = ITEMS.register("palm_chest_boat",
            () -> new BoatItem(true, ModBoats.PALM.getValue(), new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
