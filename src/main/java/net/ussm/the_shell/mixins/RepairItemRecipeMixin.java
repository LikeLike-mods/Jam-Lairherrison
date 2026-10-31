package net.ussm.the_shell.mixins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.items.ConchItem;
import net.ussm.the_shell.items.LightsaberItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// kyber tools and the conch only recharge with a crystal, not by merging two of them on a crafting table
@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {
    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void the_shell$noMerge(CraftingInput input, Level level, CallbackInfoReturnable<Boolean> callback) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() instanceof LightsaberItem || stack.getItem() instanceof ConchItem) {
                callback.setReturnValue(false);
                return;
            }
        }
    }
}
