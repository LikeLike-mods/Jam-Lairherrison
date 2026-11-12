package net.ussm.the_shell.blocks.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

// power reaching a block through cables (hanging ones too): generator = 4, solar panel in the sun = 1
public final class Energy {
    private Energy() {
    }

    public static int power(Level level, BlockPos start) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        seen.add(start);
        todo.add(start);
        int power = 0;
        while (!todo.isEmpty() && seen.size() < 512) {
            BlockPos p = todo.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (!seen.add(n)) continue;
                BlockState s = level.getBlockState(n);
                if (s.getBlock() instanceof PowerCableBlock || s.getBlock() instanceof CableBlock) todo.add(n);   // hanging cables carry it too
                else if (s.getBlock() instanceof FuelGeneratorBlock gen) power += gen.output(level, n, s);
                else if (s.getBlock() instanceof SolarPanelBlock && SolarPanelBlock.shining(level, n)) power += 1;
            }
        }
        return power;
    }
}
