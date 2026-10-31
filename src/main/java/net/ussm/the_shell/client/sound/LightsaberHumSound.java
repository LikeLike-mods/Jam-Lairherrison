package net.ussm.the_shell.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.ussm.the_shell.init.ModSounds;
import net.ussm.the_shell.items.LightsaberItem;

final class LightsaberHumSound extends AbstractTickableSoundInstance {
    private final LivingEntity wielder;

    LightsaberHumSound(LivingEntity wielder) {
        super(ModSounds.LIGHTSABER_HUM.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.wielder = wielder;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.45F;
        updatePosition();
    }

    static boolean canPlayFor(LivingEntity wielder) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity listener = minecraft.getCameraEntity();
        return minecraft.level == wielder.level() && listener != null
                && listener.distanceToSqr(wielder) < 256.0
                && !wielder.isRemoved() && wielder.isAlive() && !wielder.isSilent()
                && !(wielder instanceof Player player && player.isSpectator())
                && (isActiveLightsaber(wielder.getMainHandItem()) || isActiveLightsaber(wielder.getOffhandItem()));
    }

    private static boolean isActiveLightsaber(ItemStack stack) {
        return stack.getItem() instanceof LightsaberItem && LightsaberItem.isActive(stack);
    }

    @Override
    public boolean canPlaySound() {
        return canPlayFor(this.wielder);
    }

    @Override
    public void tick() {
        if (!canPlaySound()) {
            stop();
            return;
        }
        updatePosition();
    }

    private void updatePosition() {
        this.x = this.wielder.getX();
        this.y = this.wielder.getY();
        this.z = this.wielder.getZ();
    }
}
