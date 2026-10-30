package net.ussm.the_shell.mixins.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.ussm.the_shell.entites.Submarine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void the_shell$pilote(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity.getVehicle() instanceof Submarine sub)) return;
        Vec3 v = sub.getDeltaMovement();
        Vec3 ahead = Vec3.directionFromRotation(0.0F, sub.getYRot());
        float forward = (float) Mth.clamp(v.dot(ahead) / 0.35, -1.0, 1.0);
        float side = (float) Mth.clamp(v.dot(new Vec3(ahead.z, 0.0, -ahead.x)) / 0.2, -1.0, 1.0);
        float shake = Mth.sin(ageInTicks * 0.6F) * 0.04F * Math.abs(forward);
        this.rightArm.xRot = -1.1F - forward * 0.3F + shake;
        this.leftArm.xRot = -1.1F - forward * 0.3F - shake;
        this.rightArm.yRot = -0.3F + side * 0.15F;
        this.leftArm.yRot = 0.3F + side * 0.15F;
        this.rightArm.zRot = side * 0.15F;
        this.leftArm.zRot = side * 0.15F;
        this.body.xRot = forward * 0.1F;
        this.head.zRot = -side * 0.1F;
    }
}
