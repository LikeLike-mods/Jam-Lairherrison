package net.ussm.examplemod.blocks;

import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.material.MapColor;

public class PalmLeaves extends TransparentBlock
{
    public PalmLeaves(Properties properties) {
        super(properties.of().mapColor(MapColor.COLOR_GREEN));
    }
}

