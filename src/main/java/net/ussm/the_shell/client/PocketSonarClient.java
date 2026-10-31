package net.ussm.the_shell.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import net.ussm.the_shell.TheShell;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class PocketSonarClient {
    private static final List<int[]> POINTS = new ArrayList<>();
    private static long end;

    public static void ping(Player player) {
        POINTS.clear();
        Level level = player.level();
        BlockPos c = player.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-16, -16, -16), c.offset(16, 16, 16))) {
            BlockState st = level.getBlockState(p);
            int colour = 0;
            String entryName = st.getBlock().getDescriptionId();
            if (st.is(Tags.Blocks.ORES) || entryName.contains("kyber_ore")) colour = colour(entryName);
            else if (st.hasBlockEntity() && level.getBlockEntity(p) instanceof Container) colour = 0xFFE8B84A;
            if (colour != 0) {
                POINTS.add(new int[]{p.getX(), p.getY(), p.getZ(), colour});
                if (POINTS.size() >= 300) break;
            }
        }
        end = level.getGameTime() + 160;
    }

    private static int colour(String entryName) {
        if (entryName.contains("kyber")) return 0xFF8CF4FF;
        if (entryName.contains("diamond")) return 0xFF4FE6E0;
        if (entryName.contains("emerald")) return 0xFF3CE070;
        if (entryName.contains("gold")) return 0xFFF4D040;
        if (entryName.contains("redstone")) return 0xFFE8302A;
        if (entryName.contains("lapis")) return 0xFF3060E0;
        if (entryName.contains("copper")) return 0xFFE08850;
        if (entryName.contains("iron")) return 0xFFE0C8A8;
        if (entryName.contains("coal")) return 0xFF505050;
        return 0xFFFFFFFF;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || POINTS.isEmpty() || mc.level.getGameTime() > end) return;
        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(SonarLines.TYPE);
        float fade = Math.min(1.0F, (end - mc.level.getGameTime()) / 40.0F);
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        for (int[] p : POINTS) {
            int c = p[3];
            LevelRenderer.renderLineBox(pose, vc, new AABB(p[0], p[1], p[2], p[0] + 1, p[1] + 1, p[2] + 1).inflate(0.01),
                    ((c >> 16) & 255) / 255.0F, ((c >> 8) & 255) / 255.0F, (c & 255) / 255.0F, fade);
        }
        pose.popPose();
        buffers.endBatch(SonarLines.TYPE);
    }

    private static class SonarLines extends RenderType {
        private SonarLines(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sort, setup, clear);
        }

        static final RenderType TYPE = create("the_shell_sonar_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 1536, false, false,
                CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new LineStateShard(OptionalDouble.of(3.0)))
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL).setDepthTestState(NO_DEPTH_TEST).createCompositeState(false));
    }
}
