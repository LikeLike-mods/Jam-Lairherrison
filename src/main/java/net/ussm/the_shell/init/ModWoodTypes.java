package net.ussm.the_shell.init;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.ussm.the_shell.TheShell;

public class ModWoodTypes {
    public static final BlockSetType PALM_SET = BlockSetType.register(new BlockSetType(TheShell.MOD_ID + ":palm"));
    public static final WoodType PALM = WoodType.register(new WoodType(TheShell.MOD_ID + ":palm", PALM_SET));
    public static final BlockSetType DATE_PALM_SET = BlockSetType.register(new BlockSetType(TheShell.MOD_ID + ":date_palm"));
    public static final WoodType DATE_PALM = WoodType.register(new WoodType(TheShell.MOD_ID + ":date_palm", DATE_PALM_SET));
    public static final BlockSetType BANANA_SET = BlockSetType.register(new BlockSetType(TheShell.MOD_ID + ":banana"));
    public static final WoodType BANANA = WoodType.register(new WoodType(TheShell.MOD_ID + ":banana", BANANA_SET));
}
