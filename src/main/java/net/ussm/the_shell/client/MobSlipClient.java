package net.ussm.the_shell.client;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;

// a mob that slipped on a banana peel lies on its side, with stars spinning above it
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class MobSlipClient {

    private static boolean lying(LivingEntity entity) {
        return entity instanceof Mob && entity.getData(ModAttachments.SLIPPED);
    }

    @SubscribeEvent
    public static void lieDown(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity mob = event.getEntity();
        if (!lying(mob)) return;
        float facing = 180.0F - Mth.rotLerp(event.getPartialTick(), mob.yBodyRotO, mob.yBodyRot);
        event.getPoseStack().pushPose();
        event.getPoseStack().translate(0.0F, mob.getBbWidth() / 2.0F, 0.0F);
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-facing));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(90.0F));
    }

    @SubscribeEvent
    public static void getUp(RenderLivingEvent.Post<?, ?> event) {
        if (lying(event.getEntity())) event.getPoseStack().popPose();
    }

    @SubscribeEvent
    public static void stars(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;
        long time = mc.level.getGameTime();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity mob) || !lying(mob)) continue;
            for (int i = 0; i < 3; i++) {
                double angle = time * 0.35 + i * (Math.PI * 2 / 3);
                mc.level.addParticle(ParticleTypes.CRIT, mob.getX() + Math.cos(angle) * 0.5, mob.getY() + mob.getBbWidth() / 2.0 + 0.7,
                        mob.getZ() + Math.sin(angle) * 0.5, 0.0, 0.0, 0.0);
            }
        }
    }
}
