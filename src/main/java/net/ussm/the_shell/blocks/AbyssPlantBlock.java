package net.ussm.the_shell.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.state.BlockState;

// plantes du fond des abysses (vers tubicoles, anemones) : comme les herbes marines, toujours pleines d'eau,
// mais l'engrais ne les fait pas grandir et elles brillent un peu
public class AbyssPlantBlock extends SeagrassBlock {
    private final boolean glows;

    public AbyssPlantBlock(Properties properties, boolean glows) {
        super(properties);
        this.glows = glows;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (glows && random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.GLOW, pos.getX() + random.nextDouble(), pos.getY() + 0.3 + random.nextDouble() * 0.6, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }
}
