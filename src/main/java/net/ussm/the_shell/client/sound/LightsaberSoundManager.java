package net.ussm.the_shell.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.ussm.the_shell.TheShell;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public final class LightsaberSoundManager {
    private static final Map<LivingEntity, LightsaberHumSound> ACTIVE_SOUNDS = new HashMap<>();
    private static ClientLevel currentLevel;

    private LightsaberSoundManager() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        SoundManager soundManager = minecraft.getSoundManager();

        if (minecraft.level != currentLevel) {
            ACTIVE_SOUNDS.values().forEach(soundManager::stop);
            ACTIVE_SOUNDS.clear();
            currentLevel = minecraft.level;
        }

        if (minecraft.level == null || minecraft.isPaused()) return;

        ACTIVE_SOUNDS.entrySet().removeIf(entry -> {
            LightsaberHumSound sound = entry.getValue();
            if (!sound.canPlaySound() || sound.isStopped() || !soundManager.isActive(sound)) {
                soundManager.stop(sound);
                return true;
            }
            return false;
        });

        if (minecraft.options.getSoundSourceVolume(SoundSource.MASTER) <= 0.0F
                || minecraft.options.getSoundSourceVolume(SoundSource.PLAYERS) <= 0.0F) return;

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity wielder && !ACTIVE_SOUNDS.containsKey(wielder)
                    && LightsaberHumSound.canPlayFor(wielder)) {
                LightsaberHumSound sound = new LightsaberHumSound(wielder);
                ACTIVE_SOUNDS.put(wielder, sound);
                soundManager.play(sound);
            }
        }
    }
}
