package net.ussm.the_shell.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IndustrialLampBlock extends DirectionalBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<IndustrialLampBlock> CODEC = simpleCodec(IndustrialLampBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape UP = Block.box(4, 0, 4, 12, 8, 12);
    private static final VoxelShape DOWN = Block.box(4, 8, 4, 12, 16, 12);
    private static final VoxelShape NORTH = Block.box(4, 4, 8, 12, 12, 16);
    private static final VoxelShape SOUTH = Block.box(4, 4, 0, 12, 12, 8);
    private static final VoxelShape EAST = Block.box(0, 4, 4, 8, 12, 12);
    private static final VoxelShape WEST = Block.box(8, 4, 4, 16, 12, 12);
    private final boolean works;

    public IndustrialLampBlock(Properties properties, boolean works) {
        super(properties);
        this.works = works;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(WATERLOGGED, false).setValue(LIT, works).setValue(POWERED, false));
    }

    public IndustrialLampBlock(Properties properties) {
        this(properties, true);
    }

    @Override
    protected MapCodec<? extends IndustrialLampBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, LIT, POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace())
                .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!works) {
            level.playSound(player, pos, SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.4F, 1.8F);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            BlockState toggled = state.cycle(LIT);
            level.setBlock(pos, toggled, Block.UPDATE_ALL);
            level.playSound(null, pos, toggled.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.8F, 1.0F);
            onToggled(toggled, level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected void onToggled(BlockState state, Level level, BlockPos pos) {
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        boolean signal = level.hasNeighborSignal(pos);
        if (!works || signal == state.getValue(POWERED)) return;
        BlockState next = state.setValue(POWERED, signal);
        if (signal) {
            next = next.cycle(LIT);
            level.playSound(null, pos, next.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        level.setBlock(pos, next, Block.UPDATE_ALL);
        if (signal) onToggled(next, level, pos);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
