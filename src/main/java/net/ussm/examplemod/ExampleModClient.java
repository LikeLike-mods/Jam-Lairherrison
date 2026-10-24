package net.ussm.examplemod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ExampleMod.MOD_ID, dist = Dist.CLIENT)
public class ExampleModClient {
    public ExampleModClient(ModContainer container) {
        // le code client (rendus, pancartes) est dans client/ModClientEvents
    }
}
