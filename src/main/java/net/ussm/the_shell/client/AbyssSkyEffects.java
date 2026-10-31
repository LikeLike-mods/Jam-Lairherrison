package net.ussm.the_shell.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.ussm.the_shell.TheShell;

@EventBusSubscriber(modid = TheShell.MOD_ID, value = Dist.CLIENT)
public class AbyssSkyEffects {

    @SubscribeEvent
    public static void register(RegisterDimensionSpecialEffectsEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(TheShell.MOD_ID, "abyss"),
                new DimensionSpecialEffects(Float.NaN, false, DimensionSpecialEffects.SkyType.NONE, false, false) {
                    @Override
                    public Vec3 getBrightnessDependentFogColor(Vec3 colour, float brightness) {
                        return colour.scale(0.15);
                    }

                    @Override
                    public boolean isFoggyAt(int x, int y) {
                        return true;
                    }
                });
    }
}
