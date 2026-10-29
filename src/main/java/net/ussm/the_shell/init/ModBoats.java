package net.ussm.the_shell.init;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.ussm.the_shell.TheShell;

import java.util.function.Supplier;

public class ModBoats {
    public static final EnumProxy<Boat.Type> PALM = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>) () -> ModBlocks.PALM_PLANKS.get(),
            TheShell.MOD_ID + ":palm",
            (Supplier<Item>) () -> ModItems.PALM_BOAT.get(),
            (Supplier<Item>) () -> ModItems.PALM_CHEST_BOAT.get(),
            (Supplier<Item>) () -> Items.STICK,
            false);
}
