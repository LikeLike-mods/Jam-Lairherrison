package net.ussm.the_shell.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.ussm.the_shell.items.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheShell.MOD_ID);

    public static final DeferredItem<Item> SAINT_ITEM_TEST = ITEMS.register("saint_item_test",
            () -> new Item(new Item.Properties().stacksTo(1)));


    public static final DeferredItem<Item> COCONUT_SHELL = ITEMS.register("coconut_shell",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COCONUT_HALF = ITEMS.register("coconut_half",
            () -> new CoconutHalfItem(new Item.Properties().food(ModFoodProperties.COCONUT_HALF))); //Go check FoodProperties, it's WAY CLEANER

    public static final DeferredItem<Item> BANANA = ITEMS.register("banana", () -> new BananaItem(new Item.Properties().food(ModFoodProperties.BANANA)));

    public static final DeferredItem<Item> BANANA_BREAD = ITEMS.register("banana_bread", () -> new Item(new Item.Properties().food(ModFoodProperties.BANANA_BREAD)));

    public static final DeferredItem<Item> BANANA_CHIPS = ITEMS.register("banana_chips", () -> new Item(new Item.Properties().food(ModFoodProperties.BANANA_CHIPS)));

    public static final DeferredItem<Item> DATES = ITEMS.register("dates", () -> new Item(new Item.Properties().food(ModFoodProperties.DATES)));

    public static final DeferredItem<Item> DATE_SYRUP = ITEMS.register("date_syrup", () -> new HoneyBottleItem(new Item.Properties()
            .food(ModFoodProperties.DATE_SYRUP)
            .craftRemainder(Items.GLASS_BOTTLE)
            .stacksTo(16)));

    public static final DeferredItem<Item> SUBMARINE_UPGRADE_IRON = ITEMS.register("submarine_upgrade_iron", () -> new Item(new Item.Properties()
            .stacksTo(16)));

    public static final DeferredItem<Item> SUBMARINE_UPGRADE_DIAMOND = ITEMS.register("submarine_upgrade_diamond", () -> new Item(new Item.Properties()
            .stacksTo(16)));

    public static final DeferredItem<Item> SUBMARINE_UPGRADE_NETHERITE = ITEMS.register("submarine_upgrade_netherite", () -> new Item(new Item.Properties()
            .stacksTo(16)
            .fireResistant()));


    public static final DeferredItem<Item> PALM_SIGN = ITEMS.register("palm_sign",
            () -> new SignItem(new Item.Properties().stacksTo(16), ModBlocks.PALM_SIGN.get(), ModBlocks.PALM_WALL_SIGN.get()));

    public static final DeferredItem<Item> PALM_HANGING_SIGN = ITEMS.register("palm_hanging_sign",
            () -> new HangingSignItem(ModBlocks.PALM_HANGING_SIGN.get(), ModBlocks.PALM_WALL_HANGING_SIGN.get(), new Item.Properties()
                    .stacksTo(16)));

    public static final DeferredItem<Item> PALM_BOAT = ITEMS.register("palm_boat",
            () -> new BoatItem(false, ModBoats.PALM.getValue(), new Item.Properties()
                    .stacksTo(1)));

    public static final DeferredItem<Item> PALM_CHEST_BOAT = ITEMS.register("palm_chest_boat",
            () -> new BoatItem(true, ModBoats.PALM.getValue(), new Item.Properties()
                    .stacksTo(1)));




    public static final DeferredItem<Item> NECTAR = ITEMS.register("nectar",
            () -> new HoneyBottleItem(new Item.Properties()
                    .craftRemainder(Items.GLASS_BOTTLE)
                    .stacksTo(16)
                    .food(ModFoodProperties.NECTAR))); // Cleaner this way, go check ModFoodProperties

    public static final DeferredItem<Item> REDSTONE_PEPPER = ITEMS.register("redstone_pepper",
            () -> new Item(new Item.Properties().food(ModFoodProperties.REDSTONE_PEPPER)));

    public static final DeferredItem<Item> REDSTONE_PEPPER_SEEDS = ITEMS.register("redstone_pepper_seeds",
            () -> new ItemNameBlockItem(ModBlocks.REDSTONE_PEPPER_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> RAW_CRAB_MEAT = ITEMS.register("raw_crab_meat",
            () -> new Item(new Item.Properties().food(ModFoodProperties.RAW_CRAB_MEAT)));

    public static final DeferredItem<Item> COOKED_CRAB_MEAT = ITEMS.register("cooked_crab_meat",
            () -> new Item(new Item.Properties().food(ModFoodProperties.COOKED_CRAB_MEAT)));





    public static final DeferredItem<Item> ABYSSAL_SCALE = ITEMS.register("abyssal_scale",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> KYBER_CRYSTAL = ITEMS.register("kyber_crystal",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));


    public static final DeferredItem<Item> BLACK_PEARL = ITEMS.register("black_pearl",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> GRAPPLING_HOOK = ITEMS.register("grappling_hook",
            () -> new GrapplingHookItem(new Item.Properties().durability(128)));

    public static final DeferredItem<Item> OXYGEN_BOTTLE = ITEMS.register("oxygen_bottle",
            () -> new OxygenBottleItem(new Item.Properties().durability(4)));

    public static final DeferredItem<Item> POCKET_SONAR = ITEMS.register("pocket_sonar",
            () -> new PocketSonarItem(new Item.Properties().durability(96)));

    public static final DeferredItem<Item> IRON_ABYSS_HELMET = ITEMS.register("iron_abyss_helmet",
            () -> new AbyssHelmetItem(new Item.Properties()
                    .stacksTo(1)
                    .attributes(helmet("iron", 2, 0, 0)),
                    9));

    public static final DeferredItem<Item> DIAMOND_ABYSS_HELMET = ITEMS.register("diamond_abyss_helmet",
            () -> new AbyssHelmetItem(new Item.Properties()
                    .stacksTo(1)
                    .attributes(helmet("diamond", 3, 2, 0)),
                    10));

    public static final DeferredItem<Item> NETHERITE_ABYSS_HELMET = ITEMS.register("netherite_abyss_helmet",
            () -> new AbyssHelmetItem(new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .attributes(helmet("netherite", 3, 3, 0.1)),
                    15));

    public static final DeferredItem<ArmorItem> DIVING_HELMET = ITEMS.register("diving_helmet",
            () -> new ArmorItem(ModArmorMaterials.DIVING, ArmorItem.Type.HELMET, new Item.Properties()
                    .durability(ArmorItem.Type.HELMET.getDurability(15))));

    public static final DeferredItem<ArmorItem> DIVING_CHESTPLATE = ITEMS.register("diving_chestplate",
            () -> new ArmorItem(ModArmorMaterials.DIVING, ArmorItem.Type.CHESTPLATE, new Item.Properties()
                    .durability(ArmorItem.Type.CHESTPLATE.getDurability(15))));

    public static final DeferredItem<ArmorItem> DIVING_LEGGINGS = ITEMS.register("diving_leggings",
            () -> new ArmorItem(ModArmorMaterials.DIVING, ArmorItem.Type.LEGGINGS, new Item.Properties()
                    .durability(ArmorItem.Type.LEGGINGS.getDurability(15))));

    public static final DeferredItem<ArmorItem> DIVING_BOOTS = ITEMS.register("diving_boots",
            () -> new net.ussm.the_shell.items.DivingBootsItem(ModArmorMaterials.DIVING, new Item.Properties()
                    .durability(ArmorItem.Type.BOOTS.getDurability(15))));

    public static final DeferredItem<Item> DIVING_ABYSS_HELMET = ITEMS.register("diving_abyss_helmet",
            () -> new AbyssHelmetItem(new Item.Properties()
                    .stacksTo(1)
                    .attributes(helmet("diving", 2, 0, 0)),
                    9));

    public static final DeferredItem<Item> SUBMARINE = ITEMS.register("submarine",
            () -> new SubmarineItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> CONCH = ITEMS.register("conch",
            () -> new ConchItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final DeferredItem<Item> CRAB_CLAW = ITEMS.register("crab_claw",
            () -> new Item(new Item.Properties()));



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
    public static final DeferredItem<Item> YETI_CRAB_SPAWN_EGG = spawnEgg("yeti_crab", ModEntities.YETI_CRAB, 0xE8DCC8, 0xF4F4EC);
    public static final DeferredItem<Item> HATCHETFISH_SPAWN_EGG = spawnEgg("hatchetfish", ModEntities.HATCHETFISH, 0xB8C4CC, 0x7FD8FF);
    public static final DeferredItem<Item> COLOSSAL_SQUID_SPAWN_EGG = spawnEgg("colossal_squid", ModEntities.COLOSSAL_SQUID, 0x8A1E22, 0x6FD0FF);

    public static final DeferredItem<DeferredSpawnEggItem> LEVIATHAN_SPAWN_EGG = ITEMS.register("leviathan_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LEVIATHAN, 0x244D57, 0x4FE4DC,
                    new Item.Properties().rarity(Rarity.EPIC)));


    public static final DeferredItem<Item> LEVIATHAN_EGG = ITEMS.register("leviathan_egg",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> FRIED_LEVIATHAN_EGG = ITEMS.register("fried_leviathan_egg",
            () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE).food(ModFoodProperties.FRIED_LEVIATHAN_EGG)));

    public static final DeferredItem<Item> BASILOSAURUS_SPAWN_EGG = spawnEgg("basilosaurus", ModEntities.BASILOSAURUS, 0x354744, 0xC3B995);
    public static final DeferredItem<Item> BLUE_WHALE_SPAWN_EGG = spawnEgg("blue_whale", ModEntities.BLUE_WHALE, 0x365969, 0xA4C3CD);

    private static DeferredItem<Item> spawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight) {
        DeferredItem<Item> egg = ITEMS.register(name + "_spawn_egg", () -> new DeferredSpawnEggItem(type, background, highlight, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        return egg;
    }

    private static ItemAttributeModifiers helmet(String name, double armor, double toughness, double knockbackResistance) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, name + "_abyss_helmet");
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, armor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, toughness, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
        if (knockbackResistance > 0) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, knockbackResistance, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
        }
        return builder.build();
    }

    public static final DeferredItem<LightsaberItem> SHADOW_SABER_SWORD = ITEMS.register("lightsaber_shadow_sword_white",
        () -> new LightsaberItem("lightsaber_sword", "lightsaber_shadow_sword_white",
                8, -2.2F, ModItems.SHADOW_SABER_SICKLE, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> SHADOW_SABER_SICKLE = ITEMS.register("lightsaber_shadow_sickle_white",
        () -> new LightsaberItem("lightsaber_sickle", "lightsaber_shadow_sickle_white",
                6, -1.6F, ModItems.SHADOW_SABER_HAMMER, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> SHADOW_SABER_HAMMER = ITEMS.register("lightsaber_shadow_hammer_white",
        () -> new LightsaberItem("lightsaber_hammer", "lightsaber_shadow_hammer_white",
                12, -3.2F, ModItems.SHADOW_SABER_SWORD, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> LIGHT_SABER_SWORD = ITEMS.register("lightsaber_light_sword",
        () -> new LightsaberItem("lightsaber_sword", "lightsaber_light_sword",
                8, -2.2F, ModItems.LIGHT_SABER_SICKLE, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> LIGHT_SABER_SICKLE = ITEMS.register("lightsaber_light_sickle",
        () -> new LightsaberItem("lightsaber_sickle", "lightsaber_light_sickle",
                6, -1.6F, ModItems.LIGHT_SABER_HAMMER, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> LIGHT_SABER_HAMMER = ITEMS.register("lightsaber_light_hammer",
        () -> new LightsaberItem("lightsaber_hammer", "lightsaber_light_hammer",
                12, -3.2F, ModItems.LIGHT_SABER_SWORD, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> SHADOW_SABER_SWORD_VIOLET = ITEMS.register("lightsaber_shadow_sword_violet",
        () -> new LightsaberItem("lightsaber_sword", "lightsaber_shadow_sword_violet",
                8, -2.2F, ModItems.SHADOW_SABER_SICKLE_VIOLET, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> SHADOW_SABER_SICKLE_VIOLET = ITEMS.register("lightsaber_shadow_sickle_violet",
        () -> new LightsaberItem("lightsaber_sickle", "lightsaber_shadow_sickle_violet",
                6, -1.6F, ModItems.SHADOW_SABER_HAMMER_VIOLET, createLightsaberProperties()));

    public static final DeferredItem<LightsaberItem> SHADOW_SABER_HAMMER_VIOLET = ITEMS.register("lightsaber_shadow_hammer_violet",
        () -> new LightsaberItem("lightsaber_hammer", "lightsaber_shadow_hammer_violet",
                12, -3.2F, ModItems.SHADOW_SABER_SWORD_VIOLET, createLightsaberProperties()));

    public static final DeferredItem<Item> AQUA_PEARL = ITEMS.register("aqua_pearl", () -> new AquaPearlItem(new Item.Properties()
            .stacksTo(16)));

    public static final DeferredItem<LightsaberItem> KYBER_SICKLE = ITEMS.register("kyber_sickle",
        () -> new LightsaberItem("lightsaber_sickle", "kyber_sickle",
                6, -1.6F, ModItems.KYBER_AXE, createLightsaberProperties()));

    public static final DeferredItem<Item> KYBER_AXE = ITEMS.register("kyber_axe",
        () -> new KyberAxeItem(createLightsaberProperties()));

    private static Item.Properties createLightsaberProperties() {
        return new Item.Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .stacksTo(1)
                .durability(LightsaberItem.MAX_USES + 1);
    }



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
