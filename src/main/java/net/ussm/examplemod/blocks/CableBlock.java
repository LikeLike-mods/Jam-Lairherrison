package net.ussm.examplemod.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

// cables arraches : ca fait des etincelles de temps en temps
public class CableBlock extends DecorBlock {

    public CableBlock(Properties properties) {
        super(properties, Block.box(2, 0, 2, 14, 16, 14), false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) != 0) return;
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + random.nextDouble(),
                pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.0, 0.0);
        if (random.nextInt(4) == 0) {
            level.playLocalSound(pos, SoundEvents.COPPER_BULB_TURN_OFF, SoundSource.BLOCKS, 0.15F, 1.8F + random.nextFloat() * 0.2F, false);
        }
    }
}
