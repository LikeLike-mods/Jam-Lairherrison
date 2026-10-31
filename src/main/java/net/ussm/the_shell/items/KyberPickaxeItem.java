package net.ussm.the_shell.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.ussm.the_shell.init.ModItems;

public class KyberPickaxeItem extends LightsaberItem {
    public KyberPickaxeItem(Item.Properties properties) {
        super("lightsaber_pickaxe", "lightsaber_pure_pickaxe", 7, -2.8F, ModItems.PURE_SABER_AXE, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isActive(stack) && state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? 12.0F : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability);
    }

    public static void mineAround(ServerLevel level, ServerPlayer player, BlockPos center, BlockState centerState) {
        Direction.Axis axis = hitAxis(level, player, center);
        ItemStack tool = player.getMainHandItem();
        float hardness = centerState.getDestroySpeed(level, center);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos pos = switch (axis) {
                    case X -> center.offset(0, a, b);
                    case Y -> center.offset(a, 0, b);
                    case Z -> center.offset(a, b, 0);
                };
                BlockState state = level.getBlockState(pos);
                float other = state.getDestroySpeed(level, pos);
                if (state.isAir() || other < 0 || other > hardness + 3.0F || !tool.isCorrectToolForDrops(state)) continue;
                player.gameMode.destroyBlock(pos);
            }
        }
    }

    private static Direction.Axis hitAxis(ServerLevel level, ServerPlayer player, BlockPos center) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange() + 1.0));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(center)) return hit.getDirection().getAxis();
        return Direction.getNearest(player.getViewVector(1.0F)).getAxis();
    }
}
