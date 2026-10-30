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

// allonge apres une glissade : on ne marche plus, on regarde le ciel, et espace pour se relever
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class SlipClient {
    // il faut lacher espace puis rappuyer (sinon on se releve tout de suite si on sautait en glissant)
    private static boolean relache;

    // appele par la peau de banane quand le joueur local glisse
    public static void slip(net.minecraft.world.entity.player.Player player) {
        player.setData(ModAttachments.SLIPPED, true);
        relache = false;
        player.setXRot(-90.0F);
        player.xRotO = -90.0F;
        PacketDistributor.sendToServer(new SlipPayload(true));
    }

    @SubscribeEvent
    public static void bloque(MovementInputUpdateEvent event) {
        if (!event.getEntity().getData(ModAttachments.SLIPPED)) return;
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().leftImpulse = 0.0F;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void releve(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.getData(ModAttachments.SLIPPED)) return;
        if (!Minecraft.getInstance().options.keyJump.isDown()) {
            relache = true;
            return;
        }
        if (!relache) return;
        player.setData(ModAttachments.SLIPPED, false);
        PacketDistributor.sendToServer(new SlipPayload(false));
    }
}
