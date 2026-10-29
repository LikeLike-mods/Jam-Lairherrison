package net.ussm.theshell.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.ussm.theshell.blocks.MessageBottleBlock;
import net.ussm.theshell.blocks.MessageBottleBlockEntity;
import net.ussm.theshell.init.ModBlocks;

public class MessageBottleFeature extends Feature<NoneFeatureConfiguration> {
    public MessageBottleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        BlockState state = ModBlocks.MESSAGE_BOTTLE.get().defaultBlockState().setValue(MessageBottleBlock.ROTATION, context.random().nextInt(16));
        if (!state.canSurvive(level, pos)) return false;
        level.setBlock(pos, state, 2);
        if (level.getBlockEntity(pos) instanceof MessageBottleBlockEntity bottle) {
            bottle.setLootTable(MessageBottleBlock.TREASURE, context.random().nextLong());
        }
        return true;
    }
}
