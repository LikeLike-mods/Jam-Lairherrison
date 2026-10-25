package net.ussm.examplemod.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.resources.ResourceLocation;
import net.ussm.examplemod.items.AbyssHelmetItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SignItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.items.CoconutHalfItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Rarity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;
import net.ussm.examplemod.item.LightsaberItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MOD_ID);

    // Noix de coco entière : se pose (au sol ou suspendue sous des feuilles) ; un coup de hache dessus l'ouvre en deux
    public static final DeferredItem<Item> COCONUT = ITEMS.register("coconut",
            () -> new BlockItem(ModBlocks.COCONUT.get(), new Item.Properties()));
    // Coque vide : donne un bol à l'établi, brûle dans le four, se composte (déclarée avant la demi-noix qui la rend)
    public static final DeferredItem<Item> COCONUT_SHELL = ITEMS.register("coconut_shell",
            () -> new Item(new Item.Properties()));
    // Demi-noix : se boit (lait qui soigne le poison) et se mange, laisse une coque vide
    public static final DeferredItem<Item> COCONUT_HALF = ITEMS.register("coconut_half",
            () -> new CoconutHalfItem(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3)
                    .saturationModifier(0.5f)
                    .usingConvertsTo(ModItems.COCONUT_SHELL.get())
                    .build())));


    // Nectar de népenthès : se boit comme du miel, donne un super saut
    public static final DeferredItem<Item> ABYSSAL_SCALE = ITEMS.register("abyssal_scale",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DIAMOND_ABYSS_HELMET = ITEMS.register("diamond_abyss_helmet",
            () -> new AbyssHelmetItem(new Item.Properties().stacksTo(1).attributes(helmet("diamond", 3, 2, 0)), 10));
    public static final DeferredItem<Item> NETHERITE_ABYSS_HELMET = ITEMS.register("netherite_abyss_helmet",
            () -> new AbyssHelmetItem(new Item.Properties().stacksTo(1).fireResistant().attributes(helmet("netherite", 3, 3, 0.1)), 15));

    // Protection du casque-crâne, la même que le casque vanilla qu'on a fusionné dedans
    private static ItemAttributeModifiers helmet(String name, double armor, double toughness, double knockbackResistance) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, name + "_abyss_helmet");
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, armor, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, toughness, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
        if (knockbackResistance > 0) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, knockbackResistance, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD);
        }
        return builder.build();
    }

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

    // Ajouts d'Oesphy : item de test, oeuf du léviathan, sabres laser
    public static final DeferredItem<Item> SAINT_ITEM_TEST = ITEMS.register("saint_item_test",
        () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<DeferredSpawnEggItem> LEVIATHAN_SPAWN_EGG = ITEMS.register("leviathan_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.LEVIATHAN, 0xFFFFFF, 0xFFFFFF,
                new Item.Properties().rarity(Rarity.EPIC)));

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

    private static Item.Properties createLightsaberProperties() {
        return new Item.Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .stacksTo(1)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
