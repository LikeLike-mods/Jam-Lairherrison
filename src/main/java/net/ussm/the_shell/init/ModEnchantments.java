package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.TheShell;

// the enchantments themselves are in data/the_shell/enchantment, these are the keys used to read their level
public class ModEnchantments {
    public static final ResourceKey<Enchantment> REACH = key("reach");
    public static final ResourceKey<Enchantment> WINCH = key("winch");
    public static final ResourceKey<Enchantment> RESONANCE = key("resonance");
    public static final ResourceKey<Enchantment> RESERVE = key("reserve");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, name));
    }

    public static int level(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookup(Registries.ENCHANTMENT).flatMap(registry -> registry.get(key))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack)).orElse(0);
    }
}
