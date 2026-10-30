package net.ussm.the_shell.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.ussm.the_shell.events.TempleQuiz;

// l'autel de Notch : niveau 1 (facile), 2 (moyen) ou 3 (difficile). clic droit = une question, et il s'allume quand on a bon
public class NotchAltarBlock extends Block {
    public static final IntegerProperty NIVEAU = IntegerProperty.create("tier", 1, 3);
    public static final BooleanProperty RESOLU = BooleanProperty.create("solved");

    public NotchAltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NIVEAU, 1).setValue(RESOLU, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NIVEAU, RESOLU);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        // en creatif, accroupi : on change le niveau de l'autel (pour le poser dans le temple)
        if (player.isCreative() && player.isShiftKeyDown()) {
            int niveau = state.getValue(NIVEAU) % 3 + 1;
            level.setBlock(pos, state.setValue(NIVEAU, niveau).setValue(RESOLU, false), Block.UPDATE_ALL);
            player.displayClientMessage(Component.translatable("message.the_shell.notch.tier", niveau), true);
            return InteractionResult.SUCCESS;
        }
        if (state.getValue(RESOLU)) {
            player.sendSystemMessage(Component.literal("<Notch> ").append(Component.translatable("message.the_shell.notch.done").withStyle(ChatFormatting.GRAY)));
        } else {
            TempleQuiz.poser((ServerPlayer) player, pos, state.getValue(NIVEAU));
        }
        return InteractionResult.SUCCESS;
    }

    public static void resolu(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof NotchAltarBlock) level.setBlock(pos, state.setValue(RESOLU, true), Block.UPDATE_ALL);
    }
}
