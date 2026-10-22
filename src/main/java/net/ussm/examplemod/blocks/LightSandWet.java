package net.ussm.examplemod.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class LightSandWet extends Block
{
    public LightSandWet(Properties properties) {
        super(properties.of().mapColor(MapColor.SAND));
    }
}

