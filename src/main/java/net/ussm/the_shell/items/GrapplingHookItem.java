package net.ussm.the_shell.items;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModEnchantments;

public class GrapplingHookItem extends Item {
    private static final double RANGE = 32.0;

    public GrapplingHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(RANGE + 16.0 * ModEnchantments.level(level, stack, ModEnchantments.REACH))), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.fail(stack);
        stack.set(ModComponents.GRAPPLE_TARGET.get(), hit.getBlockPos());
        level.playSound(player, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 0.8F);
        if (!level.isClientSide) stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        BlockPos target = stack.get(ModComponents.GRAPPLE_TARGET.get());
        if (target == null) return;
        Vec3 start = entity.position().add(0.0, entity.getBbHeight() * 0.6, 0.0);
        Vec3 towards = Vec3.atCenterOf(target).subtract(start);
        double dist = towards.length();
        if (dist < 1.6) {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.3));
        } else {
            double speed = (entity.isInWater() ? 0.7 : 1.0) + 0.25 * ModEnchantments.level(level, stack, ModEnchantments.WINCH);
            entity.setDeltaMovement(towards.normalize().scale(Math.min(speed, dist * 0.25)));
        }
        // going up the rope takes back the height already fallen, but the hook no longer wipes fall damage
        double up = entity.getDeltaMovement().y;
        if (up > 0.0) entity.fallDistance = (float) Math.max(0.0, entity.fallDistance - up);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 10;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        stack.remove(ModComponents.GRAPPLE_TARGET.get());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}
