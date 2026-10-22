
package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import net.ussm.examplemod.init.ModBlocks;

public class LightSand extends Block implements Fallable {

    public LightSand(Properties properties) {
        super(properties.mapColor(MapColor.SAND));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && FallingBlock.isFree(neighborState)) {
            level.scheduleTick(pos, this, 2);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (FallingBlock.isFree(level.getBlockState(pos.below()))) {
            FallingBlockEntity.fall(level, pos, state);
        }
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        level.playSound(null, pos, SoundEvents.SAND_FALL, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        Block.popResource(level, pos, new ItemStack(ModBlocks.LIGHT_SAND.get()));
    }
}