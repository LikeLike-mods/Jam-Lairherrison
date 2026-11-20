package net.ussm.the_shell.entites;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.events.EncyclopedieHelper;
import net.ussm.the_shell.init.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Hatchetfish extends AbstractSchoolingFish implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Hatchetfish(EntityType<? extends Hatchetfish> type, Level level) {
        super(type, level);
    }

    @Override
    public int getMaxSchoolSize() {
        return 8;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.mobSound(this, "ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.mobSound(this, "hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.mobSound(this, "death");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 4,
                state -> state.setAndContinue(state.isMoving() ? DefaultAnimations.SWIM : DefaultAnimations.IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (EncyclopedieHelper.holdsBook(player, hand)) {
            if (!level().isClientSide) {
                EncyclopedieHelper.unlock(player, "encyclopedie/find_hatchetfish");
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }
}
