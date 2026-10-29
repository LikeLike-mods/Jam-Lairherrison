package net.ussm.the_shell.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.ussm.the_shell.items.LightsaberItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    @Shadow private ItemStack mainHandItem;
    @Shadow private float mainHandHeight;

    @Unique private int the_shell$selectedSlot = -1;
    @Unique private boolean the_shell$preserveHandHeight;
    @Unique private float the_shell$handHeightBeforeTick;

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void the_shell$skipLightsaberUseAnimation(InteractionHand hand, CallbackInfo callback) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getItemInHand(hand).getItem() instanceof LightsaberItem) {
            callback.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void the_shell$trackFormChange(CallbackInfo callback) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int selectedSlot = player.getInventory().selected;
        ItemStack heldItem = player.getMainHandItem();
        if (selectedSlot != this.the_shell$selectedSlot
                || !(heldItem.getItem() instanceof LightsaberItem)
                || player.swinging || player.isUsingItem()) {
            this.the_shell$preserveHandHeight = false;
        } else if (this.mainHandItem.getItem() instanceof LightsaberItem
                && !ItemStack.isSameItem(this.mainHandItem, heldItem)) {
            this.the_shell$preserveHandHeight = true;
        } else if (player.getAttackStrengthScale(1.0F) >= 1.0F) {
            this.the_shell$preserveHandHeight = false;
        }

        this.the_shell$selectedSlot = selectedSlot;
        this.the_shell$handHeightBeforeTick = this.mainHandHeight;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void the_shell$keepHandRaisedDuringFormChange(CallbackInfo callback) {
        if (this.the_shell$preserveHandHeight) {
            this.mainHandHeight = Math.max(this.mainHandHeight, this.the_shell$handHeightBeforeTick);
        }
    }
}
