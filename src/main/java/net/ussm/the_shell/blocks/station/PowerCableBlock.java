package net.ussm.the_shell.blocks.station;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.ussm.the_shell.TheShell;

// same shape as a pipe, but carries power and only joins cables and powered blocks
public class PowerCableBlock extends MetalPipeBlock {
    public static final TagKey<Block> ENERGY = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "energy_connectable"));

    public PowerCableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean connectsTo(BlockState state) {
        return state.is(this) || state.is(ENERGY);
    }
}
