package net.ussm.the_shell.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// grille d'aeration : sous l'eau elle souffle des bulles qui font remonter (la poussee est dans DeepSeaEvents)
public class AirVentBlock extends Block {

    public AirVentBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.getFluidState(pos.above()).is(FluidTags.WATER)) return;
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.05,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.04, 0.0);
        }
    }
}
