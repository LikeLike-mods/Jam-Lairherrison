package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class PalmLog extends Block
{
    public PalmLog(Properties properties) {
        super(properties.of().mapColor(MapColor.TERRACOTTA_BROWN));
    }
}
