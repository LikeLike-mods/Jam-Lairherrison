package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.ussm.the_shell.blocks.DecorBlock;

public class TankBlock extends DecorBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    // oxygen left inside, refills slowly on random ticks
    public static final int MAX_FILL = 8;
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, MAX_FILL);

    public TankBlock(Properties properties) {
        super(properties, Block.box(1, 0, 1, 15, 16, 15));
        this.registerDefaultState(this.defaultBlockState().setValue(UP, false).setValue(DOWN, false).setValue(FILL, MAX_FILL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP, DOWN, FILL);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(FILL) < MAX_FILL;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(FILL, state.getValue(FILL) + 1), Block.UPDATE_CLIENTS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return super.getStateForPlacement(context)
                .setValue(UP, context.getLevel().getBlockState(pos.above()).is(this))
                .setValue(DOWN, context.getLevel().getBlockState(pos.below()).is(this));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState out = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (direction == Direction.UP) return out.setValue(UP, neighborState.is(this));
        if (direction == Direction.DOWN) return out.setValue(DOWN, neighborState.is(this));
        return out;
    }
}
