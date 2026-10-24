package net.ussm.examplemod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.ussm.examplemod.item.LightsaberItem;
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

    @Unique private int examplemod$selectedSlot = -1;
    @Unique private boolean examplemod$preserveHandHeight;
    @Unique private float examplemod$handHeightBeforeTick;

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void examplemod$skipLightsaberUseAnimation(InteractionHand hand, CallbackInfo callback) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getItemInHand(hand).getItem() instanceof LightsaberItem) {
            callback.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void examplemod$trackFormChange(CallbackInfo callback) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        int selectedSlot = player.getInventory().selected;
        ItemStack heldItem = player.getMainHandItem();
        if (selectedSlot != this.examplemod$selectedSlot
                || !(heldItem.getItem() instanceof LightsaberItem)
                || player.swinging || player.isUsingItem()) {
            this.examplemod$preserveHandHeight = false;
        } else if (this.mainHandItem.getItem() instanceof LightsaberItem
                && !ItemStack.isSameItem(this.mainHandItem, heldItem)) {
            this.examplemod$preserveHandHeight = true;
        } else if (player.getAttackStrengthScale(1.0F) >= 1.0F) {
            this.examplemod$preserveHandHeight = false;
        }

        this.examplemod$selectedSlot = selectedSlot;
        this.examplemod$handHeightBeforeTick = this.mainHandHeight;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void examplemod$keepHandRaisedDuringFormChange(CallbackInfo callback) {
        if (this.examplemod$preserveHandHeight) {
            this.mainHandHeight = Math.max(this.mainHandHeight, this.examplemod$handHeightBeforeTick);
        }
    }
}
