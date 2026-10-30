package net.ussm.the_shell.blocks;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TempleSealBlock extends Block {
    public static final IntegerProperty NIVEAU = IntegerProperty.create("tier", 0, 3);

    public TempleSealBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NIVEAU, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NIVEAU);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isCreative() || !player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            int niveau = (state.getValue(NIVEAU) + 1) % 4;
            level.setBlock(pos, state.setValue(NIVEAU, niveau), Block.UPDATE_CLIENTS);
            player.displayClientMessage(Component.translatable("message.the_shell.seal.tier." + niveau), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from, boolean moving) {
        if (level instanceof ServerLevel serveur && level.hasNeighborSignal(pos)) ouvrir(serveur, pos);
    }

    public static void ouvrir(ServerLevel level, BlockPos depart) {
        int niveau = level.getBlockState(depart).getValue(NIVEAU);
        Set<BlockPos> vus = new HashSet<>();
        ArrayDeque<BlockPos> file = new ArrayDeque<>();
        file.add(depart);
        vus.add(depart);
        while (!file.isEmpty() && vus.size() < 512) {
            BlockPos p = file.poll();
            for (Direction d : Direction.values()) {
                BlockPos v = p.relative(d);
                BlockState s = level.getBlockState(v);
                if (!vus.contains(v) && s.getBlock() instanceof TempleSealBlock && s.getValue(NIVEAU) == niveau) {
                    vus.add(v);
                    file.add(v);
                }
            }
        }
        for (BlockPos p : vus) {
            level.levelEvent(2001, p, Block.getId(level.getBlockState(p)));
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.playSound(null, depart, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.6F);
    }

    public static void ouvrirNiveau(ServerLevel level, BlockPos autel, int niveau) {
        for (BlockPos p : BlockPos.betweenClosed(autel.offset(-24, -16, -24), autel.offset(24, 24, 24))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof TempleSealBlock && s.getValue(NIVEAU) == niveau) ouvrir(level, p.immutable());
        }
    }
}
