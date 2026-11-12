package net.ussm.the_shell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.blocks.DecorBlock;
import net.ussm.the_shell.blocks.station.SubmarineChargerBlock;
import net.ussm.the_shell.entites.Submarine;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.items.ChargingPlugItem;
import org.joml.Matrix4f;

// the charger cable, drawn like a lead: charger to the hand holding the plug, or to the plugged submarine
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class ChargerCable {
    private static final float WIDTH = 0.045F;

    @SubscribeEvent
    public static void cables(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        boolean drawn = false;
        for (Entity e : mc.level.entitiesForRendering()) {
            BlockPos pos = null;
            Vec3 end = null;
            if (e instanceof Submarine sub && sub.getCharger() != null) {
                pos = sub.getCharger();
                end = sub.getPosition(partial).add(0.0, sub.getBbHeight() * 0.5, 0.0);
            } else if (e instanceof Player player) {
                for (InteractionHand hand : InteractionHand.values()) {
                    ItemStack held = player.getItemInHand(hand);
                    if (!(held.getItem() instanceof ChargingPlugItem)) continue;
                    pos = held.get(ModComponents.CHARGER_POS.get());
                    float yaw = Mth.lerp(partial, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
                    float side = (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT) ? 1.0F : -1.0F;
                    end = player.getPosition(partial).add(0.0, player.getBbHeight() * 0.5, 0.0)
                            .add(new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw)).scale(0.35 * side));
                }
            }
            if (pos == null || end == null) continue;
            BlockState state = mc.level.getBlockState(pos);
            if (!(state.getBlock() instanceof SubmarineChargerBlock)) continue;
            Direction facing = state.getValue(DecorBlock.FACING);
            Vec3 start = Vec3.atBottomCenterOf(pos).add(facing.getStepX() * 0.3, 0.4, facing.getStepZ() * 0.3);
            PoseStack poses = event.getPoseStack();
            poses.pushPose();
            poses.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
            draw(buffers.getBuffer(RenderType.leash()), poses.last().pose(), end.subtract(start), LevelRenderer.getLightColor(mc.level, pos));
            poses.popPose();
            drawn = true;
        }
        if (drawn) buffers.endBatch(RenderType.leash());
    }

    private static void draw(VertexConsumer buffer, Matrix4f pose, Vec3 along, int light) {
        float x = (float) along.x, y = (float) along.y, z = (float) along.z;
        float flat = x * x + z * z;
        float k = flat > 1.0E-6F ? Mth.invSqrt(flat) * WIDTH / 2.0F : 0.0F;
        float dx = flat > 1.0E-6F ? z * k : WIDTH / 2.0F;
        float dz = flat > 1.0E-6F ? x * k : 0.0F;
        for (int i = 0; i <= 24; i++) pair(buffer, pose, x, y, z, dx, dz, light, i, WIDTH, false);
        for (int i = 24; i >= 0; i--) pair(buffer, pose, x, y, z, dx, dz, light, i, 0.0F, true);
    }

    // black cable that sags a bit, with a copper mark now and then
    private static void pair(VertexConsumer buffer, Matrix4f pose, float x, float y, float z, float dx, float dz, int light, int i, float dy, boolean reverse) {
        float f = i / 24.0F;
        float h = y > 0.0F ? y * f * f : y - y * (1.0F - f) * (1.0F - f);
        float shade = i % 2 == (reverse ? 1 : 0) ? 0.8F : 1.0F;
        boolean mark = i % 8 == 4;
        float r = (mark ? 0.72F : 0.13F) * shade, g = (mark ? 0.43F : 0.13F) * shade, b = (mark ? 0.2F : 0.15F) * shade;
        buffer.addVertex(pose, x * f - dx, h + dy, z * f + dz).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, x * f + dx, h + WIDTH - dy, z * f - dz).setColor(r, g, b, 1.0F).setLight(light);
    }
}
