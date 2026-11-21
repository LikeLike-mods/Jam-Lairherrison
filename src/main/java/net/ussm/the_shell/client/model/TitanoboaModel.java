package net.ussm.the_shell.client.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.animation.AdditiveBonePose;
import net.ussm.the_shell.entites.TitanoboaEntity;
import software.bernie.geckolib.animation.AnimationState;
import org.joml.Matrix3d;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class TitanoboaModel extends DefaultedEntityGeoModel<TitanoboaEntity> {
    private final AdditiveBonePose pose = new AdditiveBonePose();
    private final Matrix3d parentFrame = new Matrix3d();
    private final Matrix3d segmentFrame = new Matrix3d();
    private final Matrix3d localFrame = new Matrix3d();

    public TitanoboaModel() {
        super(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "titanoboa"), false);
    }

    @Override
    public void applyMolangQueries(AnimationState<TitanoboaEntity> state, double animationTime) {
        pose.clear();
        super.applyMolangQueries(state, animationTime);
    }

    @Override
    public void setCustomAnimations(TitanoboaEntity entity, long instanceId, AnimationState<TitanoboaEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        float partialTick = state.getPartialTick();
        double[] j = entity.joints(partialTick);
        double rad = Math.toRadians(180.0 - entity.visualYaw(partialTick));
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double fy = j[1] - j[4];
        double fh = Math.hypot(j[0] - j[3], j[2] - j[5]);
        pose.add(getAnimationProcessor()::getBone, "head", (float) Math.atan2(fy, fh), 0, 0);
        parentFrame.identity();
        for (int i = 0; i < TitanoboaEntity.SEGMENTS; i++) {
            double wx = j[(i + 1) * 3] - j[i * 3];
            double wy = j[(i + 1) * 3 + 1] - j[i * 3 + 1];
            double wz = j[(i + 1) * 3 + 2] - j[i * 3 + 2];
            double lx = wx * cos - wz * sin;
            double lz = wx * sin + wz * cos;
            double yaw = Math.atan2(lx, lz);
            double pitch = Math.atan2(-wy, Math.hypot(lx, lz));
            String bone = i < TitanoboaEntity.SEGMENTS - 1 ? "seg" + i : "tail";
            segmentFrame.rotationYXZ(yaw, pitch, 0);
            parentFrame.transpose(localFrame).mul(segmentFrame);
            float rotX = (float) Math.atan2(localFrame.m12, localFrame.m22);
            float rotY = (float) Math.asin(Mth.clamp(-localFrame.m02, -1.0, 1.0));
            float rotZ = (float) Math.atan2(localFrame.m01, localFrame.m00);
            pose.add(getAnimationProcessor()::getBone, bone, rotX, rotY, rotZ);
            parentFrame.set(segmentFrame);
        }
    }
}
