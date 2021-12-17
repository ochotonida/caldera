package caldera.data;

import caldera.data.recipe.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {

    public Recipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        addCauldronRecipes(consumer);
    }

    protected void addCauldronRecipes(Consumer<FinishedRecipe> consumer) {
        CauldronItemRecipeBuilder.addRecipes(consumer);
        CauldronFluidRecipeBuilder.addRecipes(consumer);
        CauldronBrewingRecipeBuilder.addRecipes(consumer);
        ItemConversionRecipeBuilder.addRecipes(consumer);
        EntityConversionRecipeBuilder.addRecipes(consumer);
    }
}
