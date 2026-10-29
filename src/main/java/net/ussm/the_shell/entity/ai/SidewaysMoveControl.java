package net.ussm.the_shell.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SidewaysMoveControl extends MoveControl {
    private float side = 1.0F;

    public SidewaysMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double dx = this.wantedX - this.mob.getX();
            double dz = this.wantedZ - this.mob.getZ();
            double dy = this.wantedY - this.mob.getY();
            if (dx * dx + dy * dy + dz * dz < 2.5000003E-7F) {
                this.stop();
                return;
            }
            float heading = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG);
            this.side = Math.abs(Mth.wrapDegrees(heading - this.mob.getYRot())) <= 90.0F ? 1.0F : -1.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.side > 0 ? heading : heading + 180.0F, 30.0F));
            this.walkSideways();
            BlockPos pos = this.mob.blockPosition();
            BlockState state = this.mob.level().getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(this.mob.level(), pos);
            if (dy > this.mob.maxUpStep() && dx * dx + dz * dz < Math.max(1.0F, this.mob.getBbWidth())
                    || !shape.isEmpty() && this.mob.getY() < shape.max(Direction.Axis.Y) + pos.getY()
                    && !state.is(BlockTags.DOORS) && !state.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.walkSideways();
            if (this.mob.onGround()) {
                this.operation = Operation.WAIT;
            }
        } else if (this.operation == Operation.WAIT) {
            this.stop();
        } else {
            super.tick();
        }
    }

    private void walkSideways() {
        float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.mob.setSpeed(speed);
        this.mob.setZza(0.0F);
        this.mob.setXxa(this.side * speed);
    }

    private void stop() {
        this.mob.setZza(0.0F);
        this.mob.setXxa(0.0F);
    }
}
