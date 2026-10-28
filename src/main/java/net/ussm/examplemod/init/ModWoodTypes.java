package net.ussm.examplemod.init;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.ussm.examplemod.ExampleMod;

// bois de palmier : sons des portes etc et textures des panneaux
public class ModWoodTypes {
    public static final BlockSetType PALM_SET = BlockSetType.register(new BlockSetType(ExampleMod.MOD_ID + ":palm"));
    public static final WoodType PALM = WoodType.register(new WoodType(ExampleMod.MOD_ID + ":palm", PALM_SET));
}
