package caldera.data.recipe;

import caldera.Caldera;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class OrderedCauldronRecipeBuilder implements FinishedRecipe {

    private boolean ordered = false;
    private final FluidIngredient fluidIngredient;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private ResourceLocation id;

    public OrderedCauldronRecipeBuilder(FluidIngredient fluidIngredient) {
        this.fluidIngredient = fluidIngredient;
    }

    public OrderedCauldronRecipeBuilder ordered() {
        this.ordered = true;
        return this;
    }

    public OrderedCauldronRecipeBuilder addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumer, String location) {
        save(consumer, new ResourceLocation(Caldera.MODID, "brew_types/" + location));
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        this.id = id;
        consumer.accept(this);
    }

    @Override
    public void serializeRecipeData(JsonObject object) {
        object.add("fluid", fluidIngredient.toJson());
        object.add("ingredients", CraftingHelper.writeIngredients(ingredients));
        object.addProperty("ordered", ordered);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
