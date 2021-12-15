package caldera.data.recipe;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public class CauldronFluidRecipeBuilder extends OrderedCauldronRecipeBuilder {

    private final FluidStack result;

    public CauldronFluidRecipeBuilder(FluidStack result, FluidIngredient fluidIngredient) {
        super(fluidIngredient);
        this.result = result;
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
    }

    public static void builder(Consumer<FinishedRecipe> consumer, FluidStack result, FluidIngredient fluidIngredient) {
        consumer.accept(new CauldronFluidRecipeBuilder(result, fluidIngredient));
    }

    @Override
    public RecipeSerializer<?> getType() {
        return ModRecipeTypes.CAULDRON_FLUID_CRAFTING_SERIALIZER.get();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject object = super.serializeRecipe();
        object.add("result", CraftingHelper.writeFluidStack(result, true, true));
        return object;
    }
}
