package net.ussm.the_shell.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// la peau de banane posee par terre : qui marche dessus glisse (joueurs et mobs). accroupi on passe sans glisser
public class BananaPeelBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 1, 14);

    public BananaPeelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity vivant) || vivant.isShiftKeyDown() || !vivant.onGround()) return;
        // le joueur bouge de son cote (client), les mobs cote serveur
        boolean joueur = entity instanceof Player;
        if (joueur ? !(level.isClientSide && ((Player) entity).isLocalPlayer()) : level.isClientSide) return;
        Vec3 v = entity.getDeltaMovement();
        if (v.horizontalDistanceSqr() < 0.0009) return;
        Vec3 dir = new Vec3(v.x, 0.0, v.z).normalize();
        entity.setDeltaMovement(dir.x * 1.1, 0.25, dir.z * 1.1);
        if (!joueur) {
            entity.hurtMarked = true;
            entity.setYRot(entity.getYRot() + 120.0F + level.random.nextFloat() * 120.0F);
        }
        level.playSound(joueur ? (Player) entity : null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 1.4F);
    }
}
