package net.ussm.examplemod.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class GreyGranite extends Block
{
    public GreyGranite(Properties properties) {
        super(properties.of().mapColor(MapColor.TERRACOTTA_BROWN));
    }
}