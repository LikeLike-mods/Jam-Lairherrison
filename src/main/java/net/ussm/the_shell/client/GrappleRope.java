package net.ussm.the_shell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.items.GrapplingHookItem;
import org.joml.Matrix4f;

// drawn like a lead, hand to hooked block
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class GrappleRope {

    @SubscribeEvent
    public static void rope(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        boolean drawn = false;
        for (Player player : mc.level.players()) {
            if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof GrapplingHookItem)) continue;
            BlockPos target = player.getUseItem().get(ModComponents.GRAPPLE_TARGET.get());
            if (target == null) continue;
            float yaw = Mth.lerp(partial, player.yRotO, player.getYRot()) * Mth.DEG_TO_RAD;
            Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
            Vec3 start = player.getPosition(partial).add(0.0, player.getBbHeight() * 0.62, 0.0).add(right.scale(0.3));
            Vec3 along = Vec3.atCenterOf(target).subtract(start);
            int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(start));
            PoseStack poses = event.getPoseStack();
            poses.pushPose();
            poses.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
            draw(buffers.getBuffer(RenderType.leash()), poses.last().pose(), along, light);
            poses.popPose();
            drawn = true;
        }
        if (drawn) buffers.endBatch(RenderType.leash());
    }

    private static void draw(VertexConsumer buffer, Matrix4f pose, Vec3 along, int light) {
        float x = (float) along.x, y = (float) along.y, z = (float) along.z;
        float flat = x * x + z * z;
        float k = flat > 1.0E-6F ? Mth.invSqrt(flat) * 0.025F / 2.0F : 0.0F;
        float dx = flat > 1.0E-6F ? z * k : 0.0125F;
        float dz = flat > 1.0E-6F ? x * k : 0.0F;
        for (int i = 0; i <= 24; i++) pair(buffer, pose, x, y, z, dx, dz, light, i, 0.025F, false);
        for (int i = 24; i >= 0; i--) pair(buffer, pose, x, y, z, dx, dz, light, i, 0.0F, true);
    }

    private static void pair(VertexConsumer buffer, Matrix4f pose, float x, float y, float z, float dx, float dz, int light, int i, float dy, boolean reverse) {
        float f = i / 24.0F;
        float shade = i % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;
        float r = 0.5F * shade, g = 0.4F * shade, b = 0.3F * shade;
        buffer.addVertex(pose, x * f - dx, y * f + dy, z * f + dz).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, x * f + dx, y * f + 0.025F - dy, z * f - dz).setColor(r, g, b, 1.0F).setLight(light);
    }
}
