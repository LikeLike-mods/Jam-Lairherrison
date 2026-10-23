package net.ussm.examplemod.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Unbreakable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.item.LightsaberItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExampleMod.MOD_ID);


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
