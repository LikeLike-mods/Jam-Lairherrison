package net.ussm.the_shell.items;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModSounds;

// la hache au kyber : la meme techno que les sabres (lame qui s'allume, lueur, incassable).
// allumee elle coupe vite tout ce qui se coupe a la hache, et une buche cassee fait tomber tout l'arbre (accroupi : un seul bloc)
public class KyberAxeItem extends LightsaberItem {
    public KyberAxeItem(Item.Properties properties) {
        super("kyber_axe", "kyber_axe", 9, -3.0F, () -> null, properties);
    }

    // clic droit allume / eteint (pas d'autres formes comme les sabres)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.consume(stack);
        boolean active = !isActive(stack);
        stack.set(ModComponents.ACTIVE, active);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                active ? ModSounds.LIGHTSABER_IGNITE.get() : ModSounds.LIGHTSABER_EXTINGUISH.get(), SoundSource.PLAYERS, 0.6F, 0.8F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isActive(stack) && state.is(BlockTags.MINEABLE_WITH_AXE) ? 14.0F : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability);
    }

    // tout l'arbre : les buches qui se touchent (diagonales comprises), puis les feuilles naturelles autour
    public static void abattre(ServerLevel level, BlockPos depart, Player player) {
        Set<BlockPos> buches = new HashSet<>();
        ArrayDeque<BlockPos> file = new ArrayDeque<>();
        file.add(depart);
        buches.add(depart);
        while (!file.isEmpty() && buches.size() < 256) {
            BlockPos p = file.poll();
            for (BlockPos v : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                if (Math.abs(v.getX() - depart.getX()) > 12 || Math.abs(v.getZ() - depart.getZ()) > 12 || v.getY() < depart.getY()) continue;
                if (!buches.contains(v) && level.getBlockState(v).is(BlockTags.LOGS)) {
                    BlockPos copie = v.immutable();
                    buches.add(copie);
                    file.add(copie);
                }
            }
        }
        Set<BlockPos> feuilles = new HashSet<>();
        ArrayDeque<BlockPos> bord = new ArrayDeque<>(buches);
        for (int pas = 0; pas < 7 && feuilles.size() < 600; pas++) {
            ArrayDeque<BlockPos> suivant = new ArrayDeque<>();
            for (BlockPos p : bord) {
                for (BlockPos v : new BlockPos[]{p.above(), p.below(), p.north(), p.south(), p.east(), p.west()}) {
                    BlockState s = level.getBlockState(v);
                    if (!feuilles.contains(v) && s.is(BlockTags.LEAVES) && !(s.hasProperty(LeavesBlock.PERSISTENT) && s.getValue(LeavesBlock.PERSISTENT))) {
                        feuilles.add(v);
                        suivant.add(v);
                    }
                }
            }
            bord = suivant;
        }
        buches.remove(depart);
        for (BlockPos p : buches) level.destroyBlock(p, true, player);
        for (BlockPos p : feuilles) level.destroyBlock(p, true, player);
        level.playSound(null, depart, ModSounds.LIGHTSABER_EXTINGUISH.get(), SoundSource.PLAYERS, 1.0F, 0.6F);
    }
}
