package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

// machine = pompe a air : allumee, les joueurs sous l'eau a 6 blocs autour retrouvent tout leur air,
// et l'air part dans les tuyaux branches dessus : chaque grille d'aeration au bout fait respirer a 4 blocs autour d'elle
public class MachineBlock extends DecorBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MachineBlock(Properties properties) {
        super(properties, Shapes.block());
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockState toggled = state.cycle(POWERED);
            level.setBlock(pos, toggled, Block.UPDATE_ALL);
            boolean on = toggled.getValue(POWERED);
            level.playSound(null, pos, on ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 1.4F);
            if (on) level.scheduleTick(pos, this, 20);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (state.getValue(POWERED) && !oldState.is(this)) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        giveAir(level, new AABB(pos).inflate(6.0));
        for (BlockPos vent : connectedVents(level, pos)) giveAir(level, new AABB(vent).inflate(4.0));
        level.scheduleTick(pos, this, 20);
    }

    private static void giveAir(Level level, AABB area) {
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (player.isEyeInFluid(FluidTags.WATER)) player.setAirSupply(player.getMaxAirSupply());
        }
    }

    // on suit les tuyaux depuis la machine (256 blocs max) et on garde les grilles collees a eux
    private static Set<BlockPos> connectedVents(Level level, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        Set<BlockPos> vents = new HashSet<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        todo.add(start);
        seen.add(start);
        while (!todo.isEmpty() && seen.size() < 256) {
            BlockPos p = todo.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (!seen.add(n)) continue;
                BlockState s = level.getBlockState(n);
                if (s.getBlock() instanceof MetalPipeBlock) todo.add(n);
                else if (s.getBlock() instanceof AirVentBlock) vents.add(n);
            }
        }
        return vents;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        if (random.nextInt(30) == 0) {
            level.playLocalSound(pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.3F, 0.7F, false);
        }
        if (state.getValue(WATERLOGGED) || level.getFluidState(pos.above()).is(FluidTags.WATER)) {
            for (int i = 0; i < 2; i++) {
                level.addParticle(ParticleTypes.BUBBLE, pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(), 0.0, 0.08, 0.0);
            }
        }
    }
}
