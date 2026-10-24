package net.ussm.examplemod.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

    // Oeufs d'apparition des mobs (couleurs prises sur leurs textures)
    public static final List<DeferredItem<Item>> SPAWN_EGGS = new ArrayList<>();
    public static final DeferredItem<Item> BUTTERFLY_SPAWN_EGG = spawnEgg("butterfly", ModEntities.BUTTERFLY, 0x2E7BE6, 0x1A1A1A);
    public static final DeferredItem<Item> CRAB_SPAWN_EGG = spawnEgg("crab", ModEntities.CRAB, 0xC4502A, 0xEECC9E);
    public static final DeferredItem<Item> GECKO_SPAWN_EGG = spawnEgg("gecko", ModEntities.GECKO, 0x5AAA46, 0xEC842E);
    public static final DeferredItem<Item> HERMIT_CRAB_SPAWN_EGG = spawnEgg("hermit_crab", ModEntities.HERMIT_CRAB, 0xD9A066, 0xB04A2A);
    public static final DeferredItem<Item> JELLYFISH_SPAWN_EGG = spawnEgg("jellyfish", ModEntities.JELLYFISH, 0xE678C8, 0xFFD2F0);
    public static final DeferredItem<Item> POISON_FROG_SPAWN_EGG = spawnEgg("poison_frog", ModEntities.POISON_FROG, 0x286EE6, 0x161418);
    public static final DeferredItem<Item> SEAGULL_SPAWN_EGG = spawnEgg("seagull", ModEntities.SEAGULL, 0xEEEEE8, 0x8C9098);
    public static final DeferredItem<Item> TOUCAN_SPAWN_EGG = spawnEgg("toucan", ModEntities.TOUCAN, 0x1C1A1E, 0xF48C28);
    public static final DeferredItem<Item> ABYSSAL_ANGLER_SPAWN_EGG = spawnEgg("abyssal_angler", ModEntities.ABYSSAL_ANGLER, 0x38282E, 0x80FFE2);
    public static final DeferredItem<Item> GULPER_EEL_SPAWN_EGG = spawnEgg("gulper_eel", ModEntities.GULPER_EEL, 0x14111C, 0xFF6298);
    public static final DeferredItem<Item> DRAGONFISH_SPAWN_EGG = spawnEgg("dragonfish", ModEntities.DRAGONFISH, 0x100E16, 0x6EC8FF);
    public static final DeferredItem<Item> GIANT_ISOPOD_SPAWN_EGG = spawnEgg("giant_isopod", ModEntities.GIANT_ISOPOD, 0x988EA4, 0xC87CFF);
    public static final DeferredItem<Item> VAMPIRE_SQUID_SPAWN_EGG = spawnEgg("vampire_squid", ModEntities.VAMPIRE_SQUID, 0x600E24, 0xEC282C);

    private static DeferredItem<Item> spawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight) {
        DeferredItem<Item> egg = ITEMS.register(name + "_spawn_egg", () -> new DeferredSpawnEggItem(type, background, highlight, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        return egg;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
