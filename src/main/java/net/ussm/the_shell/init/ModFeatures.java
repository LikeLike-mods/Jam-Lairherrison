package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.worldgen.feature.BeachRockFeature;
import net.ussm.the_shell.worldgen.feature.AbyssBlobFeature;
import net.ussm.the_shell.worldgen.feature.FallenLogFeature;
import net.ussm.the_shell.worldgen.feature.FruitTreeFeature;
import net.ussm.the_shell.worldgen.feature.HydrothermalChimneyFeature;
import net.ussm.the_shell.worldgen.feature.MessageBottleFeature;
import net.ussm.the_shell.worldgen.feature.PalmFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TheShell.MOD_ID);

    public static final DeferredHolder<Feature<?>, FruitTreeFeature> BANANA = FEATURES.register("banana",
            () -> new FruitTreeFeature(NoneFeatureConfiguration.CODEC, false));
    public static final DeferredHolder<Feature<?>, FruitTreeFeature> DATE_PALM = FEATURES.register("date_palm",
            () -> new FruitTreeFeature(NoneFeatureConfiguration.CODEC, true));
    public static final DeferredHolder<Feature<?>, PalmFeature> PALM = FEATURES.register("palm",
            () -> new PalmFeature(NoneFeatureConfiguration.CODEC, false));
    public static final DeferredHolder<Feature<?>, PalmFeature> PALM_WITH_VINES = FEATURES.register("palm_with_vines",
            () -> new PalmFeature(NoneFeatureConfiguration.CODEC, true));
    public static final DeferredHolder<Feature<?>, FallenLogFeature> DRIFTWOOD = FEATURES.register("driftwood",
            () -> new FallenLogFeature(NoneFeatureConfiguration.CODEC, 2, 4, false));
    public static final DeferredHolder<Feature<?>, FallenLogFeature> FALLEN_PALM = FEATURES.register("fallen_palm",
            () -> new FallenLogFeature(NoneFeatureConfiguration.CODEC, 4, 6, true));
    public static final DeferredHolder<Feature<?>, BeachRockFeature> BEACH_ROCK = FEATURES.register("beach_rock",
            () -> new BeachRockFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, MessageBottleFeature> MESSAGE_BOTTLE = FEATURES.register("message_bottle",
            () -> new MessageBottleFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, HydrothermalChimneyFeature> HYDROTHERMAL_CHIMNEY =
            FEATURES.register("hydrothermal_chimney", () -> new HydrothermalChimneyFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, AbyssBlobFeature> ABYSS_BLOB = FEATURES.register("abyss_blob",
            () -> new AbyssBlobFeature(SimpleBlockConfiguration.CODEC));
}
