package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

// projecteur, bande lumineuse : clic droit ou impulsion de redstone pour allumer/eteindre (comme l'ampoule en cuivre)
public class LightDecorBlock extends DecorBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public LightDecorBlock(Properties properties, VoxelShape north, boolean collides) {
        super(properties, north, collides);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, true).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT, POWERED);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) toggle(state.cycle(LIT), level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean movedByPiston) {
        boolean signal = level.hasNeighborSignal(pos);
        if (signal == state.getValue(POWERED)) return;
        BlockState next = state.setValue(POWERED, signal);
        if (signal) {
            toggle(next.cycle(LIT), level, pos);
        } else {
            level.setBlock(pos, next, Block.UPDATE_ALL);
        }
    }

    private void toggle(BlockState toggled, Level level, BlockPos pos) {
        level.setBlock(pos, toggled, Block.UPDATE_ALL);
        level.playSound(null, pos, toggled.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.8F, 1.0F);
        onToggled(toggled, level, pos);
    }

    protected void onToggled(BlockState state, Level level, BlockPos pos) {
    }
}
