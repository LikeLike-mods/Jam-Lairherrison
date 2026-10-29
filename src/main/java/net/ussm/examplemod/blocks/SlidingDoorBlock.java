package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// porte coulissante de 2 blocs (clic droit ou redstone), animee avec geckolib. la cassee reste coincee
public class SlidingDoorBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE_NS = Block.box(0, 0, 7, 16, 16, 9);
    private static final VoxelShape SHAPE_EW = Block.box(7, 0, 0, 9, 16, 16);
    private static final int USE_COOLDOWN = 12;   // le temps de l'animation
    private final boolean broken;

    public SlidingDoorBlock(Properties properties, boolean broken) {
        super(properties);
        this.broken = broken;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(OPEN, false).setValue(POWERED, false).setValue(WATERLOGGED, false));
    }

    public boolean isBroken() {
        return broken;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, OPEN, POWERED, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EW : SHAPE_NS;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(OPEN) ? Shapes.empty() : getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }
        boolean powered = !broken && (level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above()));
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(OPEN, powered).setValue(POWERED, powered)
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockPos above = pos.above();
        level.setBlock(above, state.setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(WATERLOGGED, level.getFluidState(above).getType() == Fluids.WATER), Block.UPDATE_ALL);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) return true;
        BlockState below = level.getBlockState(pos.below());
        return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    // les deux moities restent pareilles, et si une part l'autre part aussi (seule celle du bas donne la porte, cf le loot)
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            if (neighborState.is(this) && neighborState.getValue(HALF) != half) {
                return state.setValue(OPEN, neighborState.getValue(OPEN)).setValue(POWERED, neighborState.getValue(POWERED));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // en creatif, casser le haut ne doit pas faire tomber la porte du bas
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState lower = level.getBlockState(below);
            if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockState replace = lower.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(below, replace, Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_ALL);
                level.levelEvent(player, 2001, below, Block.getId(lower));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        // on attend qu'elle ait fini de glisser avant de pouvoir recliquer (sinon on pouvait la spammer)
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (level.getBlockEntity(lowerPos) instanceof SlidingDoorBlockEntity door) {
            if (level.getGameTime() - door.lastUse < USE_COOLDOWN) return InteractionResult.CONSUME;
            door.lastUse = level.getGameTime();
        }
        if (broken) {
            level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.5F);
            level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.25F, 1.6F);
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            if (level.getBlockEntity(lower) instanceof SlidingDoorBlockEntity door) {
                door.triggerAnim("door", "jammed");
            }
            return InteractionResult.CONSUME;
        }
        BlockState toggled = state.cycle(OPEN);
        level.setBlock(pos, toggled, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
        playSound(level, pos, toggled.getValue(OPEN));
        level.gameEvent(player, toggled.getValue(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        if (broken) return;
        BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(other);
        if (!this.defaultBlockState().is(block) && powered != state.getValue(POWERED)) {
            if (powered != state.getValue(OPEN)) playSound(level, pos, powered);
            level.setBlock(pos, state.setValue(POWERED, powered).setValue(OPEN, powered), Block.UPDATE_CLIENTS);
        }
    }

    private void playSound(Level level, BlockPos pos, boolean open) {
        level.playSound(null, pos, open ? SoundEvents.PISTON_CONTRACT : SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 1.4F);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return state.getValue(OPEN);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // seule la moitie du bas a le block entity, elle dessine toute la porte
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new SlidingDoorBlockEntity(pos, state) : null;
    }
}
