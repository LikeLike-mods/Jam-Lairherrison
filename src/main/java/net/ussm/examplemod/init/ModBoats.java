package net.ussm.examplemod.init;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.ussm.examplemod.ExampleMod;

import java.util.function.Supplier;

// Bateau en palmier : NeoForge ajoute ce type à Boat.Type (voir META-INF/enumextensions.json), donc le bateau,
// son rendu et son modèle sont ceux du jeu ; la texture est textures/entity/boat/palm.png (et chest_boat/palm.png).
public class ModBoats {
    public static final EnumProxy<Boat.Type> PALM = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>) () -> ModBlocks.PALM_PLANKS.get(),
            ExampleMod.MOD_ID + ":palm",
            (Supplier<Item>) () -> ModItems.PALM_BOAT.get(),
            (Supplier<Item>) () -> ModItems.PALM_CHEST_BOAT.get(),
            (Supplier<Item>) () -> Items.STICK,
            false);
}
