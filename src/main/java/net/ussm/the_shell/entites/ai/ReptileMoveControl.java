package net.ussm.the_shell.entites.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class ReptileMoveControl extends MoveControl {
    private final float landTurn;
    private final float waterTurn;
    private final float landSpeed;
    private final float waterSpeed;

    public ReptileMoveControl(Mob mob, float landTurn, float waterTurn, float landSpeed, float waterSpeed) {
        super(mob);
        this.landTurn = landTurn;
        this.waterTurn = waterTurn;
        this.landSpeed = landSpeed;
        this.waterSpeed = waterSpeed;
    }

    @Override
    public void tick() {
        if (operation != Operation.MOVE_TO) {
            stop();
            return;
        }
        operation = Operation.WAIT;
        skipReachedNode();
        double dx = wantedX - mob.getX();
        double dy = wantedY - mob.getY();
        double dz = wantedZ - mob.getZ();
        double flat = dx * dx + dz * dz;
        if (flat < 2.5E-7) {
            stop();
            return;
        }
        boolean water = mob.isInWater();
        float target = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        mob.setYRot(rotlerp(mob.getYRot(), target, water ? waterTurn : landTurn));
        mob.yBodyRot = mob.getYRot();
        float off = Math.abs(Mth.wrapDegrees(target - mob.getYRot()));
        float forward = Mth.clamp(1.0F - off / 150.0F, 0.35F, 1.0F);
        float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED))
                * (water ? waterSpeed : landSpeed) * forward;
        mob.setSpeed(speed);
        mob.setYya(0.0F);
        if (!water && dy > mob.maxUpStep() && flat < Math.max(1.0F, mob.getBbWidth() * mob.getBbWidth())) {
            mob.getJumpControl().jump();
        }
    }

    private void stop() {
        mob.setSpeed(0.0F);
        mob.setXxa(0.0F);
        mob.setYya(0.0F);
        mob.setZza(0.0F);
    }

    private void skipReachedNode() {
        Path path = mob.getNavigation().getPath();
        if (path == null || path.isDone() || path.getNextNodeIndex() >= path.getNodeCount() - 1) return;
        Vec3 node = path.getNextEntityPos(mob);
        double dx = node.x - mob.getX();
        double dz = node.z - mob.getZ();
        if (dx * dx + dz * dz < 6.25 && Math.abs(node.y - mob.getY()) < 1.5) path.advance();
    }
}
