package net.ussm.the_shell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.ussm.the_shell.TheShell;
import net.ussm.the_shell.recipes.ReinforceDivingSuitRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TheShell.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, ReinforceDivingSuitRecipe.Serializer> REINFORCE_DIVING_SUIT =
            SERIALIZERS.register("reinforce_diving_suit", ReinforceDivingSuitRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
