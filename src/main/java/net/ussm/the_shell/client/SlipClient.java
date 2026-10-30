package net.ussm.the_shell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.init.ModAttachments;
import net.ussm.the_shell.network.SlipPayload;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SlipClient {
    private static boolean released;

    public static void slip(net.minecraft.world.entity.player.Player player) {
        player.setData(ModAttachments.SLIPPED, true);
        released = false;
        player.setXRot(-90.0F);
        player.xRotO = -90.0F;
        PacketDistributor.sendToServer(new SlipPayload(true));
    }

    @SubscribeEvent
    public static void blocking(MovementInputUpdateEvent event) {
        if (!event.getEntity().getData(ModAttachments.SLIPPED)) return;
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().leftImpulse = 0.0F;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void woke(ClientTickEvent.Post event) { // Do you love Wokeboard ? (Wokist wakeboard)
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.getData(ModAttachments.SLIPPED)) return;
        if (!Minecraft.getInstance().options.keyJump.isDown()) {
            released = true;
            return;
        }
        if (!released) return;
        player.setData(ModAttachments.SLIPPED, false);
        PacketDistributor.sendToServer(new SlipPayload(false));
    }
}
