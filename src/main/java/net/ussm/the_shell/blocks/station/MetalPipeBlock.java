package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ussm.the_shell.TheShell;

import java.util.HashMap;
import java.util.Map;

public class MetalPipeBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final TagKey<Block> CONNECTABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "pipe_connectable"));
    private static final Map<Direction, BooleanProperty> SIDES = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final Map<Direction, VoxelShape> ARMS = Map.of(
            Direction.NORTH, Block.box(4, 4, 0, 12, 12, 5), Direction.SOUTH, Block.box(4, 4, 11, 12, 12, 16),
            Direction.WEST, Block.box(0, 4, 4, 5, 12, 12), Direction.EAST, Block.box(11, 4, 4, 16, 12, 12),
            Direction.DOWN, Block.box(4, 0, 4, 12, 5, 12), Direction.UP, Block.box(4, 11, 4, 12, 16, 12));
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public MetalPipeBlock(Properties properties) {
        super(properties);
        BlockState state = this.stateDefinition.any().setValue(WATERLOGGED, false);
        for (BooleanProperty side : SIDES.values()) state = state.setValue(side, false);
        this.registerDefaultState(state);
        for (BlockState s : this.stateDefinition.getPossibleStates()) {
            VoxelShape shape = CORE;
            for (Direction d : Direction.values()) {
                if (s.getValue(SIDES.get(d))) shape = Shapes.or(shape, ARMS.get(d));
            }
            shapes.put(s, shape);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH, BlockStateProperties.WEST,
                BlockStateProperties.UP, BlockStateProperties.DOWN, WATERLOGGED);
    }

    private static boolean connectsTo(BlockState state) {
        return state.getBlock() instanceof MetalPipeBlock || state.is(CONNECTABLE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = this.defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(pos).getType() == Fluids.WATER);
        for (Direction d : Direction.values()) {
            state = state.setValue(SIDES.get(d), connectsTo(context.getLevel().getBlockState(pos.relative(d))));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state.setValue(SIDES.get(direction), connectsTo(neighborState));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState out = state;
        for (Direction d : Direction.Plane.HORIZONTAL) out = out.setValue(SIDES.get(rotation.rotate(d)), state.getValue(SIDES.get(d)));
        return out;
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState out = state;
        for (Direction d : Direction.Plane.HORIZONTAL) out = out.setValue(SIDES.get(mirror.mirror(d)), state.getValue(SIDES.get(d)));
        return out;
    }
}
