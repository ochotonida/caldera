package caldera.data.builder;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public class CauldronFluidRecipeBuilder extends OrderedCauldronRecipeBuilder {

    private final FluidStack result;

    public CauldronFluidRecipeBuilder(FluidStack result, FluidIngredient fluidIngredient) {
        super(fluidIngredient);
        this.result = result;
    }

    public static void addRecipes(Consumer<IFinishedRecipe> consumer) {
    }

    public static CauldronFluidRecipeBuilder builder(FluidStack result, FluidIngredient fluidIngredient) {
        return new CauldronFluidRecipeBuilder(result, fluidIngredient);
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return ModRecipeTypes.CAULDRON_FLUID_CRAFTING_SERIALIZER.get();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject object = super.serializeRecipe();
        object.add("result", CraftingHelper.writeFluidStack(result, true, true));
        return object;
    }
}
