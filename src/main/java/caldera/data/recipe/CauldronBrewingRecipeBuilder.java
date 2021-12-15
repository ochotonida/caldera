package caldera.data.recipe;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public class CauldronBrewingRecipeBuilder extends OrderedCauldronRecipeBuilder {

    private final ResourceLocation result;

    public CauldronBrewingRecipeBuilder(ResourceLocation result, FluidIngredient fluidIngredient) {
        super(fluidIngredient);
        this.result = result;
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        builder(new ResourceLocation(Caldera.MODID, "test_brew"), FluidIngredient.of(new FluidStack(Fluids.WATER, 2)))
                .addIngredient(Ingredient.of(Items.BLAZE_POWDER))
                .addIngredient(Ingredient.of(Tags.Items.GUNPOWDER))
                .save(consumer, "test_brew");
    }

    public static CauldronBrewingRecipeBuilder builder(ResourceLocation result, FluidIngredient fluidIngredient) {
        return new CauldronBrewingRecipeBuilder(result, fluidIngredient);
    }

    @Override
    public RecipeSerializer<?> getType() {
        return ModRecipeTypes.CAULDRON_BREWING_SERIALIZER.get();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject object = super.serializeRecipe();
        object.addProperty("result", result.toString());
        return object;
    }
}
