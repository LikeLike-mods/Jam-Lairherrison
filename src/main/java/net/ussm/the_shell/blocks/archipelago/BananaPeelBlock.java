package net.ussm.the_shell.blocks.archipelago;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        if (!(entity instanceof LivingEntity living) || living.isShiftKeyDown() || !living.onGround() || entity.getData(net.ussm.the_shell.init.ModAttachments.SLIPPED)) return;
        boolean player = entity instanceof Player;
        if (player ? !(level.isClientSide && ((Player) entity).isLocalPlayer()) : level.isClientSide) return;
        Vec3 v = entity.getDeltaMovement();
        if (v.horizontalDistanceSqr() < 0.0009) return;
        Vec3 dir = new Vec3(v.x, 0.0, v.z).normalize();
        entity.setDeltaMovement(dir.x * 1.1, 0.25, dir.z * 1.1);
        if (player) {
            net.ussm.the_shell.client.SlipClient.slip((Player) entity);
        } else {
            entity.hurtMarked = true;
            if (entity instanceof net.minecraft.world.entity.Mob) net.ussm.the_shell.events.SlipEvents.slipMob(living);
        }
        level.playSound(player ? (Player) entity : null, pos, net.ussm.the_shell.init.ModSounds.BANANA_SLIP.get() /*Not a Banana split*/, player ? SoundSource.PLAYERS : SoundSource.NEUTRAL, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
    }
}
