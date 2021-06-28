package caldera.data;

import caldera.data.builder.CauldronFluidRecipeBuilder;
import caldera.data.builder.CauldronItemRecipeBuilder;
import caldera.data.builder.SludgeBrewTypeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

import java.util.function.Consumer;

public class Recipes extends RecipeProvider {

    public Recipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        addCauldronRecipes(consumer);
    }

    protected void addCauldronRecipes(Consumer<IFinishedRecipe> consumer) {
        SludgeBrewTypeBuilder.addRecipes(consumer);
        CauldronItemRecipeBuilder.addRecipes(consumer);
        CauldronFluidRecipeBuilder.addRecipes(consumer);
    }
}
