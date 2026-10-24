package net.ussm.examplemod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.init.ModAttachments;
import net.ussm.examplemod.init.ModBlocks;
import net.ussm.examplemod.worldgen.TestChunkGenerator;

@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class ModGameEvents {
    private static final TagKey<Biome> SPAWNS_WARM_VARIANT_FARM_ANIMALS = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "spawns_warm_variant_farm_animals"));

    // Écorcer le palmier à la hache, comme les bûches vanilla (on garde l'orientation)
    @SubscribeEvent
    public static void stripPalm(BlockEvent.BlockToolModificationEvent event) {
        if (event.getItemAbility() != ItemAbilities.AXE_STRIP) return;
        BlockState state = event.getState();
        Block stripped = null;
        if (state.is(ModBlocks.PALM_LOG.get())) stripped = ModBlocks.STRIPPED_PALM_LOG.get();
        else if (state.is(ModBlocks.PALM_WOOD.get())) stripped = ModBlocks.STRIPPED_PALM_WOOD.get();
        if (stripped != null && state.hasProperty(RotatedPillarBlock.AXIS)) {
            event.setFinalState(stripped.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        }
    }

    // Le terrain de l'atoll dépend de la graine du monde (lancé avant la création des dimensions)
    @SubscribeEvent
    public static void terrainSeed(ServerAboutToStartEvent event) {
        TestChunkGenerator.setSeed(event.getServer().getWorldData().worldGenOptions().seed());
    }

    // Vaches, cochons et poules de l'île : variante chaude selon le biome où ils apparaissent (génération, apparition naturelle, œuf)
    @SubscribeEvent
    public static void warmVariantOnSpawn(FinalizeSpawnEvent event) {
        if (hasWarmVariant(event.getEntity())) {
            setWarmVariant(event.getEntity(), event.getLevel(), BlockPos.containing(event.getX(), event.getY(), event.getZ()));
        }
    }

    // Poussins des œufs lancés, /summon : pas de FinalizeSpawnEvent, on regarde le biome à leur arrivée
    @SubscribeEvent
    public static void warmVariantOnJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!event.getLevel().isClientSide && !event.loadedFromDisk() && hasWarmVariant(entity) && !entity.hasData(ModAttachments.WARM_VARIANT)) {
            setWarmVariant(entity, event.getLevel(), entity.blockPosition());
        }
    }

    // Le petit prend la variante d'un des deux parents, comme en 1.21.5
    @SubscribeEvent
    public static void warmVariantOnBreed(BabyEntitySpawnEvent event) {
        AgeableMob child = event.getChild();
        if (child != null && hasWarmVariant(child)) {
            Entity parent = child.getRandom().nextBoolean() ? event.getParentA() : event.getParentB();
            child.setData(ModAttachments.WARM_VARIANT, parent.getData(ModAttachments.WARM_VARIANT));
        }
    }

    private static boolean hasWarmVariant(Entity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.COW || type == EntityType.PIG || type == EntityType.CHICKEN;
    }

    private static void setWarmVariant(Entity entity, LevelReader level, BlockPos pos) {
        entity.setData(ModAttachments.WARM_VARIANT, level.getBiome(pos).is(SPAWNS_WARM_VARIANT_FARM_ANIMALS));
    }
}
