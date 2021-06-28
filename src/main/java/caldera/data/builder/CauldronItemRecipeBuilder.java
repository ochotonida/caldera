package caldera.data.builder;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;

import java.util.function.Consumer;

public class CauldronItemRecipeBuilder extends OrderedCauldronRecipeBuilder {

    private final ItemStack result;

    public CauldronItemRecipeBuilder(ItemStack result, FluidIngredient fluidIngredient) {
        super(fluidIngredient);
        this.result = result;
    }

    public static void addRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    public static CauldronItemRecipeBuilder builder(ItemStack result, FluidIngredient fluidIngredient) {
        return new CauldronItemRecipeBuilder(result, fluidIngredient);
    }

    @Override
    public IRecipeSerializer<?> getType() {
        return ModRecipeTypes.CAULDRON_ITEM_CRAFTING_SERIALIZER.get();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject object = super.serializeRecipe();
        object.add("result", CraftingHelper.writeItemStack(result));
        return object;
    }
}
