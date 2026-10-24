package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

// Fleur de l'atoll : pousse sur la terre comme les fleurs vanilla, mais aussi sur le sable de la plage.
public class TropicalFlowerBlock extends FlowerBlock {
    public TropicalFlowerBlock(Holder<MobEffect> stewEffect, float seconds, Properties properties) {
        super(stewEffect, seconds, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SAND) || super.mayPlaceOn(state, level, pos);
    }
}
