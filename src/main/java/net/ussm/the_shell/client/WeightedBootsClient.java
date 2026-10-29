package net.ussm.the_shell.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.event.DropWeightPayload;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.items.DivingBootsItem;
import org.lwjgl.glfw.GLFW;

// bottes lestees cote joueur : la touche pour lacher le lingot et la texture lestee (le saut est dans DeepSeaEvents)
@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class WeightedBootsClient {
    public static final KeyMapping DROP_WEIGHT = new KeyMapping("key.the_shell.drop_weight", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.the_shell");

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent event) {
        event.register(DROP_WEIGHT);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.DIVING_BOOTS.get(), ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "weighted"),
                (stack, level, entity, seed) -> DivingBootsItem.isWeighted(stack) ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean lestees = DivingBootsItem.isWeighted(player.getItemBySlot(EquipmentSlot.FEET));
        while (DROP_WEIGHT.consumeClick()) {
            if (lestees && player.isInWater()) PacketDistributor.sendToServer(new DropWeightPayload());
        }
    }
}
