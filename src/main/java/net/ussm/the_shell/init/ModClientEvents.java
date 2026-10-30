package net.ussm.the_shell.init;

import net.minecraft.client.renderer.Sheets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.client.renderers.*;
import net.ussm.the_shell.entites.animations.SwimmingTurnProfiles;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> Sheets.addWoodType(ModWoodTypes.PALM));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MESSAGE_BOTTLE.get(), MessageBottleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TEMPLE_PEDESTAL.get(), net.ussm.the_shell.client.renderers.TemplePedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SLIDING_DOOR.get(), context -> new SlidingDoorRenderer());
        event.registerEntityRenderer(ModEntities.LEVIATHAN.get(), LeviathanRenderer::new);
        event.registerEntityRenderer(ModEntities.SUBMARINE.get(), SubmarineRenderer::new);
        event.registerEntityRenderer(ModEntities.AQUA_PEARL.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BASILOSAURUS.get(), c -> new CetaceanRenderer<>(c, "basilosaurus",
                net.ussm.the_shell.entites.BasilosaurusEntity.SIZE_MULTIPLIER, 1.5F, SwimmingTurnProfiles.BASILOSAURUS));
        event.registerEntityRenderer(ModEntities.BLUE_WHALE.get(), c -> new CetaceanRenderer<>(c, "blue_whale",
                net.ussm.the_shell.entites.BlueWhaleEntity.SIZE_MULTIPLIER,
                1.4F * net.ussm.the_shell.entites.BlueWhaleEntity.GEOMETRY_SCALE, SwimmingTurnProfiles.BLUE_WHALE));
        event.registerEntityRenderer(ModEntities.PURUSSAURUS.get(), PurussaurusRenderer::new);
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), c -> new MobRenderers<>(c, "butterfly", 0.2F, false, false, false));
        event.registerEntityRenderer(ModEntities.CRAB.get(), CrabRenderer::new);
        event.registerEntityRenderer(ModEntities.GECKO.get(), c -> new MobRenderers<>(c, "gecko", 0.25F, true, false, false));
        event.registerEntityRenderer(ModEntities.HERMIT_CRAB.get(), c -> new MobRenderers<>(c, "hermit_crab", 0.3F, false, false, false));
        event.registerEntityRenderer(ModEntities.JELLYFISH.get(), c -> new MobRenderers<>(c, "jellyfish", 0.0F, false, false, true));
        event.registerEntityRenderer(ModEntities.POISON_FROG.get(), c -> new MobRenderers<>(c, "poison_frog", 0.2F, true, false, false));
        event.registerEntityRenderer(ModEntities.SEAGULL.get(), c -> new MobRenderers<>(c, "seagull", 0.3F, true, false, false));
        event.registerEntityRenderer(ModEntities.TOUCAN.get(), c -> new MobRenderers<>(c, "toucan", 0.3F, true, false, false));
        event.registerEntityRenderer(ModEntities.ABYSSAL_ANGLER.get(), c -> new MobRenderers<>(c, "abyssal_angler", 0.0F, false, true, false, MobRenderers.SwimPose.PITCH));
        event.registerEntityRenderer(ModEntities.GULPER_EEL.get(), c -> new MobRenderers<>(c, "gulper_eel", 0.0F, false, true, false, MobRenderers.SwimPose.PITCH));
        event.registerEntityRenderer(ModEntities.DRAGONFISH.get(), c -> new MobRenderers<>(c, "dragonfish", 0.0F, false, true, false, MobRenderers.SwimPose.PITCH));
        event.registerEntityRenderer(ModEntities.GIANT_ISOPOD.get(), c -> new MobRenderers<>(c, "giant_isopod", 0.6F, false, true, false));
        event.registerEntityRenderer(ModEntities.VAMPIRE_SQUID.get(), c -> new MobRenderers<>(c, "vampire_squid", 0.0F, false, true, false, MobRenderers.SwimPose.LEAN));
        event.registerEntityRenderer(ModEntities.YETI_CRAB.get(), c -> new MobRenderers<>(c, "yeti_crab", 0.4F, false, false, false));
        event.registerEntityRenderer(ModEntities.HATCHETFISH.get(), c -> new MobRenderers<>(c, "hatchetfish", 0.15F, false, true, false, MobRenderers.SwimPose.PITCH));
        event.registerEntityRenderer(ModEntities.COLOSSAL_SQUID.get(), c -> new MobRenderers<>(c, "colossal_squid", 1.0F, false, true, false, MobRenderers.SwimPose.PITCH));
    }
}
