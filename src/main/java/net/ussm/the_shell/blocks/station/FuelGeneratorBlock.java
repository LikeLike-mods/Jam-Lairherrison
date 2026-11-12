package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.ussm.the_shell.blocks.DecorBlock;
import net.ussm.the_shell.entites.Submarine;

public class FuelGeneratorBlock extends DecorBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int MAX_FUEL = 15;
    public static final IntegerProperty FUEL = IntegerProperty.create("fuel", 0, MAX_FUEL);   // minutes left
    private static final int MINUTE = 1200;

    public FuelGeneratorBlock(Properties properties) {
        super(properties, Shapes.block());
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, false).setValue(FUEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, FUEL);
    }

    // same fuels as the submarine: coal or redstone = 2 min, kyber or a redstone block = full
    public static int minutes(ItemStack stack) {
        int value = Submarine.fuelValue(stack);
        return value <= 0 ? 0 : Math.max(1, Math.min(MAX_FUEL, value / 75));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int add = minutes(stack);
        if (add == 0) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            if (state.getValue(FUEL) >= MAX_FUEL) {
                player.displayClientMessage(Component.translatable("message.the_shell.generator.full"), true);
            } else {
                int fuel = Math.min(MAX_FUEL, state.getValue(FUEL) + add);
                level.setBlock(pos, state.setValue(FUEL, fuel).setValue(LIT, true), Block.UPDATE_ALL);
                if (!level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, MINUTE);
                stack.consume(1, player);
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.6F, 1.2F);
                player.displayClientMessage(Component.translatable("message.the_shell.generator.fuel", fuel), true);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.the_shell.generator.fuel", state.getValue(FUEL)), true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        int fuel = state.getValue(FUEL) - 1;
        level.setBlock(pos, state.setValue(FUEL, Math.max(0, fuel)).setValue(LIT, fuel > 0), Block.UPDATE_ALL);
        if (fuel > 0) level.scheduleTick(pos, this, MINUTE);
        else level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    // a generator placed by worldgen has no tick yet, so it gets one the first time something asks for power
    public int output(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(LIT)) return 0;
        if (!level.isClientSide && !level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, MINUTE);
        return 4;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        if (random.nextInt(12) == 0) level.playLocalSound(pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 0.8F, false);
        level.addParticle(state.getValue(WATERLOGGED) ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.SMOKE,
                pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05, pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.04, 0.0);
    }
}
