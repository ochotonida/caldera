package caldera.common.recipe.cauldron;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ItemResultRecipe;
import caldera.common.recipe.ItemResultRecipeSerializer;
import caldera.common.recipe.ingredient.FluidIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class CauldronItemRecipe extends OrderedCauldronRecipe<ItemStack> implements ItemResultRecipe {

    private final ItemStack result;

    public CauldronItemRecipe(ResourceLocation id, ItemStack result, boolean isOrdered, FluidIngredient fluidIngredient, List<Ingredient> ingredients) {
        super(id, isOrdered, fluidIngredient, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public ItemStack assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_ITEM_CRAFTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CauldronRecipe<ItemStack>> getType() {
        return ModRecipeTypes.CAULDRON_ITEM_CRAFTING;
    }

    public static class Serializer extends OrderedCauldronRecipe.Serializer<CauldronItemRecipe, ItemStack> implements ItemResultRecipeSerializer<CauldronItemRecipe> {

        @Override
        public CauldronItemRecipe createRecipe(
                ResourceLocation id,
                ItemStack result,
                boolean isOrdered,
                FluidIngredient fluidIngredient,
                List<Ingredient> ingredients
        ) {
            return new CauldronItemRecipe(id, result, isOrdered, fluidIngredient, ingredients);
        }
    }
}
