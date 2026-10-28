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

// projecteur, bande lumineuse : clic droit pour allumer/eteindre
public class LightDecorBlock extends DecorBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LightDecorBlock(Properties properties, VoxelShape north, boolean collides) {
        super(properties, north, collides);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockState toggled = state.cycle(LIT);
            level.setBlock(pos, toggled, Block.UPDATE_ALL);
            level.playSound(null, pos, toggled.getValue(LIT) ? SoundEvents.COPPER_BULB_TURN_ON : SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
