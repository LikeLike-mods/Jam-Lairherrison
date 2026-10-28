package net.ussm.examplemod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.ussm.examplemod.ExampleMod;
import net.ussm.examplemod.blocks.AbyssLightBlock;
import net.ussm.examplemod.init.ModBlocks;

@EventBusSubscriber(modid = ExampleMod.MOD_ID)
public class ModGameEvents {
    // ecorcer le palmier
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

    // lumiere du crane quand on le porte
    @SubscribeEvent
    public static void headLight(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || player.tickCount % 4 != 0 || !AbyssLightBlock.wearsLight(player)) return;
        AbyssLightBlock.placeAt(player.level(), BlockPos.containing(player.getEyePosition()));
    }
}
