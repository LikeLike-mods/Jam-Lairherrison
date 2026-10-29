package net.ussm.theshell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.theshell.TheShell;
import net.ussm.theshell.worldgen.feature.BeachRockFeature;
import net.ussm.theshell.worldgen.feature.FallenLogFeature;
import net.ussm.theshell.worldgen.feature.MessageBottleFeature;
import net.ussm.theshell.worldgen.feature.PalmFeature;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TheShell.MOD_ID);

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
}
