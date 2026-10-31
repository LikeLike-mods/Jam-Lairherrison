package net.ussm.the_shell.mixins;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.items.KyberSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// a kyber crystal can replace the lapis: the table then draws from every enchantment (mending included, no curses)
// and the enchantments that roll their top level go one level beyond it
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Shadow @Final private Container enchantSlots;
    @Shadow @Final private RandomSource random;
    @Shadow @Final private DataSlot enchantmentSeed;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void the_shell$kyberSlot(int id, Inventory inventory, ContainerLevelAccess access, CallbackInfo callback) {
        EnchantmentMenu menu = (EnchantmentMenu) (Object) this;
        KyberSlot slot = new KyberSlot(this.enchantSlots, 1, 35, 47);
        slot.index = 1;
        menu.slots.set(1, slot);
    }

    @Inject(method = "getEnchantmentList", at = @At("HEAD"), cancellable = true)
    private void the_shell$kyberEnchantments(RegistryAccess registries, ItemStack stack, int slot, int cost, CallbackInfoReturnable<List<EnchantmentInstance>> callback) {
        if (!this.enchantSlots.getItem(1).is(ModItems.KYBER_CRYSTAL.get())) return;
        this.random.setSeed((long) (this.enchantmentSeed.get() + slot));
        Stream<Holder<Enchantment>> pool = registries.registryOrThrow(Registries.ENCHANTMENT).holders()
                .filter(holder -> !holder.is(EnchantmentTags.CURSE)).map(holder -> (Holder<Enchantment>) holder);
        List<EnchantmentInstance> picked = EnchantmentHelper.selectEnchantment(this.random, stack, cost * 3 / 2, pool);
        if (stack.is(Items.BOOK) && picked.size() > 1) picked.remove(this.random.nextInt(picked.size()));
        List<EnchantmentInstance> result = new ArrayList<>();
        for (EnchantmentInstance instance : picked) {
            int max = instance.enchantment.value().getMaxLevel();
            result.add(max > 1 && instance.level >= max ? new EnchantmentInstance(instance.enchantment, max + 1) : instance);
        }
        callback.setReturnValue(result);
    }
}
