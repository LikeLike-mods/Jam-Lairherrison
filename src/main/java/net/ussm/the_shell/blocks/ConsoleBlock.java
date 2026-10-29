package net.ussm.the_shell.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

// console = sonar : on appuie, ping, et toutes les creatures a 32 blocs brillent 6 secondes (on les voit a travers le noir des abysses)
public class ConsoleBlock extends DecorBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final double RANGE = 32.0;

    public ConsoleBlock(Properties properties) {
        super(properties, Block.box(1, 0, 3, 15, 11, 16));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(POWERED)) return InteractionResult.PASS;   // le sonar recharge
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
            level.scheduleTick(pos, this, 60);
            List<LivingEntity> found = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(RANGE), e -> e != player && e.isAlive());
            for (LivingEntity e : found) e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, false));
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.0F, 1.8F);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.8F, 0.6F);
            player.displayClientMessage(Component.translatable("console.the_shell.sonar", found.size()), true);
            level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && state.getValue(POWERED)) level.updateNeighborsAt(pos, this);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // pendant le ping elle envoie aussi une impulsion de redstone
    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }
}
