package net.ussm.the_shell.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.events.AtollTravel;
import net.ussm.the_shell.init.ModSounds;
import net.ussm.the_shell.worldgen.TestChunkGenerator;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class WavesSounds {
    private static Waves current;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.level.dimension() != AtollTravel.ATOLL) return;
        if (current == null || !mc.getSoundManager().isActive(current)) {
            current = new Waves();
            mc.getSoundManager().play(current);
        }
    }

    private static float seaDistance(double x, double z) {
        for (int r = 0; r <= 48; r += 6) {
            int n = r == 0 ? 1 : 8;
            for (int k = 0; k < n; k++) {
                double a = Math.PI * 2 * k / n;
                if (TestChunkGenerator.getHeight((float) (x + r * Math.cos(a)), (float) (z + r * Math.sin(a))) < TestChunkGenerator.SEA - 1) return r;
            }
        }
        return 64;
    }

    private static class Waves extends AbstractTickableSoundInstance {
        private float target;

        Waves() {
            super(ModSounds.AMBIENT_ATOLL_WAVES.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = 0.01F;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.level().dimension() != AtollTravel.ATOLL) {
                this.stop();
                return;
            }
            if (player.tickCount % 10 == 0) {
                float near = Mth.clamp(1.0F - seaDistance(player.getX(), player.getZ()) / 48.0F, 0.0F, 1.0F);
                float low = Mth.clamp(1.0F - (float) (player.getY() - TestChunkGenerator.SEA - 10) / 40.0F, 0.0F, 1.0F);
                target = player.isUnderWater() ? 0.0F : near * near * low;
            }
            this.volume += (Math.max(target, 0.001F) - this.volume) * 0.05F;
        }
    }
}
