package caldera.data.recipe;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Consumer;

public class CauldronItemRecipeBuilder extends OrderedCauldronRecipeBuilder {

    private final ItemStack result;

    public CauldronItemRecipeBuilder(ItemStack result, FluidIngredient fluidIngredient) {
        super(fluidIngredient);
        this.result = result;
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {

    }

    public static void builder(Consumer<FinishedRecipe> consumer, ItemStack result, FluidIngredient fluidIngredient) {
        consumer.accept(new CauldronItemRecipeBuilder(result, fluidIngredient));
    }

    @Override
    public RecipeSerializer<?> getType() {
        return ModRecipeTypes.CAULDRON_ITEM_CRAFTING_SERIALIZER.get();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject object = super.serializeRecipe();
        object.add("result", CraftingHelper.writeItemStack(result));
        return object;
    }
}
