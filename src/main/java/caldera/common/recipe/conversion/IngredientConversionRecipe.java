package caldera.common.recipe.conversion;

import caldera.common.recipe.SingleResultRecipeSerializer;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Predicate;

public interface IngredientConversionRecipe<RESULT, INPUT, INGREDIENT extends Predicate<INPUT>> extends ConversionRecipe<RESULT, INPUT> {

    INGREDIENT ingredient();

    @Override
    default boolean matches(INPUT input) {
        return ingredient().test(input);
    }

    abstract class Serializer<RESULT, INGREDIENT extends Predicate<?>, RECIPE extends IngredientConversionRecipe<RESULT, ?, INGREDIENT>>
            extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements SingleResultRecipeSerializer<RESULT, RECIPE> {

        public abstract RECIPE createRecipe(
                ResourceLocation id,
                ResourceLocation conversionType,
                INGREDIENT ingredient,
                RESULT result
        );

        public abstract INGREDIENT readIngredient(JsonElement element);

        public abstract INGREDIENT readIngredient(FriendlyByteBuf buffer);

        public abstract void writeIngredient(INGREDIENT ingredient, FriendlyByteBuf buffer);

        @Override
        public RECIPE fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            if (!object.has("ingredient")) {
                throw new JsonParseException("Missing ingredient");
            }
            INGREDIENT ingredient = readIngredient(object.get("ingredient"));
            RESULT result = readResult(object);

            return createRecipe(id, conversionType, ingredient, result);
        }

        @Override
        public RECIPE fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            INGREDIENT ingredient = readIngredient(buffer);
            RESULT result = readResult(buffer);

            return createRecipe(id, conversionType, ingredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RECIPE recipe) {
            buffer.writeResourceLocation(recipe.conversionType());
            writeIngredient(recipe.ingredient(), buffer);
            writeResult(buffer, recipe);
        }
    }
}
