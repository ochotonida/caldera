package caldera.data.builder;

import caldera.Caldera;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class OrderedCauldronRecipeBuilder implements IFinishedRecipe {

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

    public void build(Consumer<IFinishedRecipe> consumer, String location) {
        build(consumer, new ResourceLocation(Caldera.MODID, "brew_types/" + location));
    }

    public void build(Consumer<IFinishedRecipe> consumer, ResourceLocation id) {
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
