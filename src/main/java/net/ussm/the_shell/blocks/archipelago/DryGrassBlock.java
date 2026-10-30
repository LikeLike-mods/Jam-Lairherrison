package net.ussm.the_shell.blocks.archipelago;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DryGrassBlock extends BushBlock {
    public static final MapCodec<DryGrassBlock> CODEC = simpleCodec(DryGrassBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

    public DryGrassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.move(state.getOffset(level, pos).x, 0, state.getOffset(level, pos).z);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT);
    }
}
