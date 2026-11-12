package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.ussm.the_shell.blocks.DecorBlock;

// a tilted panel on a small post, faces where you place it
public class SolarPanelBlock extends DecorBlock {
    public SolarPanelBlock(Properties properties) {
        super(properties, Block.box(0, 0, 0, 16, 12, 16));
    }

    // works in daylight with the sky above it
    public static boolean shining(Level level, BlockPos pos) {
        return level.isDay() && level.canSeeSky(pos.above());
    }
}
