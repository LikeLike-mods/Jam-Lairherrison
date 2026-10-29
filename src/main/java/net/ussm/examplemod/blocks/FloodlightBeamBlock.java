package net.ussm.examplemod.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
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
import net.ussm.examplemod.init.ModBlocks;

// lumiere invisible du faisceau d'un projecteur, elle part toute seule si plus aucun projecteur allume ne vise ici
public class FloodlightBeamBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<FloodlightBeamBlock> CODEC = simpleCodec(FloodlightBeamBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public FloodlightBeamBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    public static void placeAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState beam = ModBlocks.FLOODLIGHT_BEAM.get().defaultBlockState();
        if (state.isAir()) {
            level.setBlock(pos, beam, Block.UPDATE_ALL);
        } else if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
            level.setBlock(pos, beam.setValue(WATERLOGGED, true), Block.UPDATE_ALL);
        }
    }

    // on remonte jusqu'au projecteur, mais un mur entre les deux coupe le faisceau (sinon ca eclairait derriere les murs)
    private static boolean lit(LevelReader level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int k = 1; k <= FloodlightBlock.RANGE; k++) {
                BlockPos p = pos.relative(d.getOpposite(), k);
                BlockState s = level.getBlockState(p);
                if (s.is(ModBlocks.FLOODLIGHT.get())) {
                    if (s.getValue(FloodlightBlock.FACING) == d && s.getValue(FloodlightBlock.LIT)) return true;
                    break;
                }
                if (s.isSolidRender(level, p)) break;
            }
        }
        return false;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
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
        return state;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        level.scheduleTick(pos, this, 40);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (lit(level, pos)) {
            level.scheduleTick(pos, this, 40);
        } else {
            level.setBlock(pos, state.getValue(WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // un peu de poussiere qui brille dans le faisceau
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
    }
}
