package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
import net.ussm.the_shell.blocks.DecorBlock;
import net.ussm.the_shell.events.DeepSeaEvents;
import net.ussm.the_shell.events.SubmarineInterior;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.init.ModBlocks;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

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
            if (!state.getValue(POWERED) && !powered(level, pos)) {
                player.displayClientMessage(Component.translatable("message.the_shell.energy.none"), true);
                return InteractionResult.SUCCESS;
            }
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
        if (!powered(level, pos)) {   // power cut
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.6F, 1.4F);
            return;
        }
        giveAir(level, new AABB(pos).inflate(6.0));
        for (BlockPos vent : connectedVents(level, pos)) giveAir(level, new AABB(vent).inflate(4.0));
        level.scheduleTick(pos, this, 20);
    }

    private static void giveAir(Level level, AABB area) {
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (!player.isEyeInFluid(FluidTags.WATER)) continue;
            player.setAirSupply(player.getMaxAirSupply());
            int missing = ModAttachments.MAX_OXYGEN - player.getData(ModAttachments.OXYGEN);
            if (missing > 0) DeepSeaEvents.giveOxygen(player, Math.min(480, missing));   // the diving suit too
        }
    }

    // in the submarine cabin it is the engine, it runs on the battery
    private static boolean powered(Level level, BlockPos pos) {
        return level.dimension() == SubmarineInterior.INTERIOR || Energy.power(level, pos) > 0;
    }

    // a turned valve fixed on a pipe shuts it
    private static boolean open(Level level, BlockPos pipe) {
        for (Direction d : Direction.values()) {
            BlockPos p = pipe.relative(d);
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof ValveBlock && s.getValue(ValveBlock.POWERED) && pipe.equals(ValveBlock.attached(s, p))) return false;
        }
        return true;
    }

    // a vent only blows when a running pump is on its pipes
    public static boolean feeds(Level level, BlockPos vent) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        seen.add(vent);
        todo.add(vent);
        while (!todo.isEmpty() && seen.size() < 256) {
            BlockPos p = todo.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (!seen.add(n)) continue;
                BlockState s = level.getBlockState(n);
                if (s.is(ModBlocks.PIPE.get())) {
                    if (open(level, n)) todo.add(n);
                }
                else if (s.getBlock() instanceof MachineBlock && s.getValue(POWERED)) return true;
            }
        }
        return false;
    }

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
                if (s.is(ModBlocks.PIPE.get())) {
                    if (open(level, n)) todo.add(n);
                }
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
