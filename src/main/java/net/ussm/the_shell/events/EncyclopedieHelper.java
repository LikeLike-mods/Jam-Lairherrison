package net.ussm.the_shell.events;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EncyclopedieHelper {


    private static final List<String> CREATURES = List.of(
            "abyssal_angler", "basilosaurus", "blue_whale", "butterfly", "colossal_squid",
            "crab", "dragonfish", "dragonfly", "firefly", "gecko", "giant_isopod",
            "gulper_eel", "hatchetfish", "hermit_crab", "jellyfish", "leviathan",
            "poison_frog", "purussaurus", "seagull", "titanoboa", "toucan",
            "vampire_squid", "yeti_crab"
    );

    public static void unlock(Player player, String advancementPath) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            //player.displayClientMessage(Component.literal("pas un ServerPlayer"), false);
            return;
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("the_shell", advancementPath);
        AdvancementHolder holder = serverPlayer.server.getAdvancements().get(id);

        if (holder == null) {
            //player.displayClientMessage(Component.literal("advancement introuvable : " + id), false);
            return;
        }

        AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(holder);

        if (progress.isDone()) {
            //player.displayClientMessage(Component.literal("deja obtenu : " + id), false);
            checkAllFound(serverPlayer);


            return;
        }

        for (String criterion : progress.getRemainingCriteria()) {
            serverPlayer.getAdvancements().award(holder, criterion);
            //player.displayClientMessage(Component.literal("award " + criterion + " = ok"), false);

        }

        checkAllFound(serverPlayer);

    }

    public static boolean holdsBook(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!itemId.equals(ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book"))) return false;

        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE
                .get(ResourceLocation.fromNamespaceAndPath("patchouli", "book"));

        if (type == null) {
            return false;
        }

        Object value = stack.get(type);

        return (value != null && value.toString().equals("the_shell:encyclopedie"));
    }


    private static void checkAllFound(ServerPlayer player) {
        //player.displayClientMessage(Component.literal("check lancé"), false);

        for (String name : CREATURES) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("the_shell", "encyclopedie/find_" + name);
            AdvancementHolder holder = player.server.getAdvancements().get(id);

            if (holder == null) {
                //player.displayClientMessage(Component.literal("introuvable : " + id), false);
                return;
            }

            if (!player.getAdvancements().getOrStartProgress(holder).isDone()){
                //player.displayClientMessage(Component.literal("manquant : " + name), false);
                return;
            }
        }
        //player.displayClientMessage(Component.literal("les 23 sont faites"), false);

        ResourceLocation finalId = ResourceLocation.fromNamespaceAndPath("the_shell", "find_all");
        AdvancementHolder finalHolder = player.server.getAdvancements().get(finalId);

        if (finalHolder == null) {
            //player.displayClientMessage(Component.literal("all_found introuvable"), false);
            return;
        }
        if (player.getAdvancements().getOrStartProgress(finalHolder).isDone()) {
            //player.displayClientMessage(Component.literal("all_found deja obtenu"), false);
            return;
        }

        unlock(player, "find_all");
    }
}
