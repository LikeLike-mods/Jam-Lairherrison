package net.ussm.the_shell.entites.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.ussm.the_shell.entites.init.CetaceanEntity;

/** Smooth heading with independent vertical thrust, including precise surface approaches. */
public final class CetaceanMoveControl extends MoveControl {
    private float yawVelocity;
    public CetaceanMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (!mob.isInWater()) {
            super.tick();
            return;
        }
        if (operation != Operation.MOVE_TO) {
            clearInput();
            yawVelocity = 0;
            mob.setXRot(Mth.approachDegrees(mob.getXRot(), 0, ((CetaceanEntity) mob).getPitchTurnPerTick()));
            return;
        }
        operation = Operation.WAIT; // A navigation path or goal must renew the target each tick.
        double dx = wantedX - mob.getX();
        double dy = wantedY - mob.getY();
        double dz = wantedZ - mob.getZ();
        double horizontal = Math.hypot(dx, dz);
        double distance = Math.sqrt(horizontal * horizontal + dy * dy);
        if (distance < 0.15) {
            clearInput();
            mob.setDeltaMovement(mob.getDeltaMovement().scale(0.5));
            return;
        }
        float heading = mob.getYRot();
        if (horizontal > 0.05) {
            heading = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
            CetaceanEntity swimmer = (CetaceanEntity) mob;
            float delta = Mth.wrapDegrees(heading - mob.getYRot());
            float desired = Mth.clamp(delta, -swimmer.getMaxTurnPerTick(), swimmer.getMaxTurnPerTick());
            yawVelocity = Mth.approach(yawVelocity, desired, swimmer.getTurnAcceleration());
            if (Math.abs(delta) < Math.abs(yawVelocity)) yawVelocity = delta;
            mob.setYRot(mob.getYRot() + yawVelocity);
        }
        mob.yBodyRot = mob.getYRot();
        mob.yHeadRot = mob.getYRot();
        float thrust = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        thrust *= (float) Mth.clamp(distance / 2.0, 0.5, 1.0);
        float alignment = Math.max(0, Mth.cos(Mth.wrapDegrees(heading - mob.getYRot()) * Mth.DEG_TO_RAD));
        mob.setSpeed(thrust * 0.08F);
        mob.setXxa(0);
        mob.setZza((float) (horizontal / distance) * thrust * alignment);
        mob.setYya((float) (dy / distance) * thrust);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        mob.setXRot(Mth.approachDegrees(mob.getXRot(), Mth.clamp(pitch, -60, 60), ((CetaceanEntity) mob).getPitchTurnPerTick()));
    }

    private void clearInput() {
        mob.setSpeed(0);
        mob.setXxa(0);
        mob.setYya(0);
        mob.setZza(0);
    }
}
