package net.ussm.examplemod.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class Brownstone extends Block
{
    public Brownstone(Properties properties) {
        super(properties.of().mapColor(MapColor.TERRACOTTA_BROWN));
    }
}

