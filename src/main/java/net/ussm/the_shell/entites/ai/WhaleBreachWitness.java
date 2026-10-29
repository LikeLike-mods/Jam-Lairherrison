package net.ussm.the_shell.entites.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.BlueWhaleEntity;
import net.ussm.the_shell.init.ModCriteriaTriggers;

/** Require active spyglass use, an airborne head/body in the scope, and an unobstructed view. */
public final class WhaleBreachWitness {
    private WhaleBreachWitness() {}

    public static void tick(BlueWhaleEntity whale) {
        if (!(whale.level() instanceof ServerLevel level) || !whale.isBreaching()
                || whale.getActivityTick() < 80 || whale.getActivityTick() > 130 || whale.getActivityTick() % 5 != 0) return;
        for (ServerPlayer player : level.players()) {
            if (canWitness(player, whale)) ModCriteriaTriggers.WITNESS_WHALE_BREACH.get().trigger(player);
        }
    }

    public static boolean canWitness(ServerPlayer player, BlueWhaleEntity whale) {
        if (!player.isAlive() || !player.isScoping() || player.level() != whale.level() || !whale.isBreaching()) return false;
        Vec3 eye = player.getEyePosition();
        Vec3 aim = eye.add(player.getViewVector(1).scale(96));
        for (var part : whale.getParts()) {
            if (!part.partName().startsWith("head") && !part.partName().startsWith("body")) continue;
            var sight = part.getBoundingBox().clip(eye, aim);
            if (sight.isEmpty()) continue;
            Vec3 point = sight.get();
            if (point.y < whale.getBreachSurfaceY() + 0.5) continue;
            if (canSee(player, point)) return true;
        }
        return false;
    }

    public static boolean canSee(ServerPlayer player, Vec3 point) {
        if (!player.isScoping()) return false;
        Vec3 eye = player.getEyePosition();
        Vec3 towards = point.subtract(eye);
        if (towards.lengthSqr() > 96 * 96 || towards.lengthSqr() < 0.0001) return false;
        if (player.getViewVector(1).dot(towards.normalize()) < Math.cos(Math.toRadians(3))) return false;
        return player.level().clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }
}
