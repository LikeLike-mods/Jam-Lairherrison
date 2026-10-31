package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SteelBeamBlock extends RotatedPillarBlock {
    public static final BooleanProperty ROTATED = BooleanProperty.create("rotated");

    private static final VoxelShape UP = Shapes.or(Block.box(2, 0, 2, 14, 16, 5), Block.box(2, 0, 11, 14, 16, 14), Block.box(6, 0, 5, 10, 16, 11));
    private static final VoxelShape UP_TURNED = Shapes.or(Block.box(2, 0, 2, 5, 16, 14), Block.box(11, 0, 2, 14, 16, 14), Block.box(5, 0, 6, 11, 16, 10));
    private static final VoxelShape ALONG_Z = Shapes.or(Block.box(2, 2, 0, 14, 5, 16), Block.box(2, 11, 0, 14, 14, 16), Block.box(6, 5, 0, 10, 11, 16));
    private static final VoxelShape ALONG_Z_TURNED = Shapes.or(Block.box(2, 2, 0, 5, 14, 16), Block.box(11, 2, 0, 14, 14, 16), Block.box(5, 6, 0, 11, 10, 16));
    private static final VoxelShape ALONG_X = Shapes.or(Block.box(0, 2, 2, 16, 5, 14), Block.box(0, 11, 2, 16, 14, 14), Block.box(0, 5, 6, 16, 11, 10));
    private static final VoxelShape ALONG_X_TURNED = Shapes.or(Block.box(0, 2, 2, 16, 14, 5), Block.box(0, 2, 11, 16, 14, 14), Block.box(0, 6, 5, 16, 10, 11));

    public SteelBeamBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ROTATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATED);
    }

    // sneak turns it when lying down
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        boolean turned = state.getValue(AXIS) == Direction.Axis.Y
                ? context.getHorizontalDirection().getAxis() == Direction.Axis.X
                : context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return state.setValue(ROTATED, turned);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) level.setBlock(pos, state.cycle(ROTATED), Block.UPDATE_ALL);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean turned = state.getValue(ROTATED);
        return switch (state.getValue(AXIS)) {
            case X -> turned ? ALONG_X_TURNED : ALONG_X;
            case Z -> turned ? ALONG_Z_TURNED : ALONG_Z;
            default -> turned ? UP_TURNED : UP;
        };
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
