package net.ussm.examplemod.init;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.ussm.examplemod.ExampleMod;

// Type de bois du palmier : sons des portes/trappes/boutons (BlockSetType) et textures des panneaux (WoodType),
// rangées dans textures/entity/signs/palm.png, textures/entity/signs/hanging/palm.png et textures/gui/hanging_signs/palm.png.
public class ModWoodTypes {
    public static final BlockSetType PALM_SET = BlockSetType.register(new BlockSetType(ExampleMod.MOD_ID + ":palm"));
    public static final WoodType PALM = WoodType.register(new WoodType(ExampleMod.MOD_ID + ":palm", PALM_SET));
}
