package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.ussm.examplemod.init.ModBlocks;
import net.ussm.examplemod.init.ModItems;

public class CoconutBlock extends Block implements Fallable {
    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    private static final VoxelShape HANGING_SHAPE = round(8);
    private static final VoxelShape GROUND_SHAPE = round(0);

    private static VoxelShape round(int y) {
        return Shapes.or(Block.box(5, y + 1, 6, 11, y + 5, 10), Block.box(6, y + 1, 5, 10, y + 5, 11), Block.box(6, y, 6, 10, y + 6, 10));
    }

    public CoconutBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HANGING);
    }

    // suspendue si on la met sous des feuilles
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean hanging = context.getClickedFace() == Direction.DOWN && hasBranch(context.getLevel(), context.getClickedPos());
        BlockState state = this.defaultBlockState().setValue(HANGING, hanging);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    // coup de hache = coco ouverte en 2
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(HANGING) || !stack.canPerformAction(ItemAbilities.AXE_DIG)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            level.destroyBlock(pos, false, player);
            Block.popResource(level, pos, new ItemStack(ModItems.COCONUT_HALF.get(), 2));
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_SHAPE : GROUND_SHAPE;
    }

    private static boolean hasBranch(LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.is(BlockTags.LEAVES) || above.is(BlockTags.LOGS);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasBranch(level, pos) || Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(HANGING)) {
            if (direction == Direction.UP && !hasBranch(level, pos)) {
                level.scheduleTick(pos, this, 2);   // plus de feuilles donc elle tombe
            }
            return state;
        }
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HANGING) && !hasBranch(level, pos)) {
            fall(level, pos, state);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HANGING);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(45) == 0 && level.isEmptyBlock(pos.below())) {
            fall(level, pos, state);
        }
    }

    private static void fall(ServerLevel level, BlockPos pos, BlockState state) {
        FallingBlockEntity coconut = FallingBlockEntity.fall(level, pos, state);
        coconut.setHurtsEntities(1.0F, 3);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        level.setBlock(pos, state.setValue(HANGING, false), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.WOOD_FALL, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        Block.popResource(level, pos, new ItemStack(ModBlocks.COCONUT.get()));
    }
}
