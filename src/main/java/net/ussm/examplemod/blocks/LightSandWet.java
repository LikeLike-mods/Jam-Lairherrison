package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.ussm.examplemod.init.ModBlocks;

public class LightSandWet extends Block implements Fallable
{
    public LightSandWet(Properties properties) {
        super(properties.of().mapColor(MapColor.SAND));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        FallingBlockEntity.fall(level, pos, state);
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

