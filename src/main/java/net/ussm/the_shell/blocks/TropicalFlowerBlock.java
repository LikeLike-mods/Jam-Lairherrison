package net.ussm.the_shell.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

// pousse aussi sur le sable
public class TropicalFlowerBlock extends FlowerBlock {
    public TropicalFlowerBlock(Holder<MobEffect> stewEffect, float seconds, Properties properties) {
        super(stewEffect, seconds, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SAND) || super.mayPlaceOn(state, level, pos);
    }
}
