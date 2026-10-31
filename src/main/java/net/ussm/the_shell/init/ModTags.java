package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.ussm.the_shell.TheShell;

public final class ModTags {
    public static final TagKey<Item> KYBER_CRYSTALS = TagKey.create(Registries.ITEM, id("kyber_crystals"));
    public static final TagKey<Item> SHADOW_KYBER_CRYSTALS = TagKey.create(Registries.ITEM, id("shadow_kyber_crystals"));
    public static final TagKey<EntityType<?>> SEA_CREATURES = TagKey.create(Registries.ENTITY_TYPE, id("sea_creatures"));

    private ModTags() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, path);
    }
}
