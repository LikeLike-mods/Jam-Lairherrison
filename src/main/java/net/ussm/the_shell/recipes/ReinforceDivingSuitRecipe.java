package net.ussm.the_shell.recipes;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.ussm.the_shell.init.ModComponents;
import net.ussm.the_shell.init.ModItems;
import net.ussm.the_shell.init.ModRecipes;
import net.ussm.the_shell.items.DivingSuitItem;

public class ReinforceDivingSuitRecipe extends ShapedRecipe {
    private final Item base;

    public ReinforceDivingSuitRecipe(Item base) {
        super("diving_suit_reinforcement", CraftingBookCategory.EQUIPMENT, pattern(base), reinforced(new ItemStack(base)));
        this.base = base;
    }

    private static ShapedRecipePattern pattern(Item base) {
        return ShapedRecipePattern.of(Map.of(
                'S', Ingredient.of(ModItems.PURUSSAURUS_SCALE.get()),
                'T', Ingredient.of(ModItems.TITANOBOA_SKIN.get()),
                'B', Ingredient.of(base)), "STS", "SBS", "STS");
    }

    private static ItemStack reinforced(ItemStack stack) {
        stack.set(ModComponents.REINFORCED, true);
        return stack;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return super.matches(input, level) && input.width() == 3 && input.height() == 3 && !DivingSuitItem.isReinforced(input.getItem(1, 1));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return reinforced(input.getItem(1, 1).copyWithCount(1));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.REINFORCE_DIVING_SUIT.get();
    }

    public static class Serializer implements RecipeSerializer<ReinforceDivingSuitRecipe> {
        private static final MapCodec<ReinforceDivingSuitRecipe> CODEC =
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("base").xmap(ReinforceDivingSuitRecipe::new, recipe -> recipe.base);
        private static final StreamCodec<RegistryFriendlyByteBuf, ReinforceDivingSuitRecipe> STREAM_CODEC =
                ByteBufCodecs.registry(Registries.ITEM).map(ReinforceDivingSuitRecipe::new, recipe -> recipe.base);

        @Override
        public MapCodec<ReinforceDivingSuitRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ReinforceDivingSuitRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
