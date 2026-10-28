package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// projecteur : eclaire loin devant lui (jusqu'a 24 blocs) avec une rangee de lumieres invisibles, s'arrete au premier mur
public class FloodlightBlock extends LightDecorBlock {
    public static final int RANGE = 24;

    public FloodlightBlock(Properties properties) {
        super(properties, Block.box(3, 0, 4, 13, 13, 12), true);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) level.scheduleTick(pos, this, 5);
    }

    @Override
    protected void onToggled(BlockState state, Level level, BlockPos pos) {
        level.scheduleTick(pos, this, 5);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        Direction facing = state.getValue(FACING);
        for (int k = 1; k <= RANGE; k++) {
            BlockPos p = pos.relative(facing, k);
            BlockState there = level.getBlockState(p);
            if (there.isSolidRender(level, p)) break;
            if (k % 3 == 2) FloodlightBeamBlock.placeAt(level, p);
        }
        level.scheduleTick(pos, this, 40);
    }
}
