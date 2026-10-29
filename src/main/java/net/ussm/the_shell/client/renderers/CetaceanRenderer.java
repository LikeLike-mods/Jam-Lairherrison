package net.ussm.the_shell.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.BlueWhaleEntity;
import net.ussm.the_shell.entites.init.CetaceanEntity;
import net.ussm.the_shell.entites.animations.TurnBendProfile;
import net.ussm.the_shell.client.model.TurningEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CetaceanRenderer<T extends CetaceanEntity> extends GeoEntityRenderer<T> {
    public CetaceanRenderer(EntityRendererProvider.Context context, String name, float scale, float shadow, TurnBendProfile turnProfile) {
        super(context, new TurningEntityGeoModel<>(name, turnProfile));
        withScale(scale);
        shadowRadius = shadow;
    }

    @Override
    public Vec3 getRenderOffset(T entity, float partialTick) {
        return new Vec3(0, entity.getModelYOffset() * entity.getScale(), 0);
    }

    @Override
    protected void applyRotations(T entity, PoseStack pose, float age, float yaw, float partialTick, float nativeScale) {
        super.applyRotations(entity, pose, age, yaw, partialTick, nativeScale);
        if (entity.isInWater() && !(entity instanceof BlueWhaleEntity whale && whale.isPerformingSurfaceActivity())) {
            float pitch = -Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            pose.mulPose(Axis.XP.rotationDegrees(pitch));
        }
    }
}
