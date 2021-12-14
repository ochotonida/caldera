package caldera.common.recipe.transmutation;

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

public interface IngredientTransmutationRecipe<RESULT, INPUT, INGREDIENT extends Predicate<INPUT>> extends TransmutationRecipe<RESULT, INPUT> {

    INGREDIENT ingredient();

    @Override
    default boolean matches(INPUT input) {
        return ingredient().test(input);
    }

    abstract class Serializer<RESULT, INGREDIENT extends Predicate<?>, RECIPE extends IngredientTransmutationRecipe<RESULT, ?, INGREDIENT>>
            extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements SingleResultRecipeSerializer<RESULT, RECIPE> {

        public abstract RECIPE createRecipe(
                ResourceLocation id,
                ResourceLocation transmutationType,
                INGREDIENT ingredient,
                RESULT result
        );

        public abstract INGREDIENT readIngredient(JsonElement element);

        public abstract INGREDIENT readIngredient(FriendlyByteBuf buffer);

        public abstract void writeIngredient(INGREDIENT ingredient, FriendlyByteBuf buffer);

        @Override
        public RECIPE fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation transmutationType = CraftingHelper.readResourceLocation(object, "transmutationType");
            if (!object.has("ingredient")) {
                throw new JsonParseException("Missing ingredient");
            }
            INGREDIENT ingredient = readIngredient(object.get("ingredient"));
            RESULT result = readResult(object);

            return createRecipe(id, transmutationType, ingredient, result);
        }

        @Override
        public RECIPE fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation transmutationType = buffer.readResourceLocation();
            INGREDIENT ingredient = readIngredient(buffer);
            RESULT result = readResult(buffer);

            return createRecipe(id, transmutationType, ingredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RECIPE recipe) {
            buffer.writeResourceLocation(recipe.transmutationType());
            writeIngredient(recipe.ingredient(), buffer);
            writeResult(buffer, recipe);
        }
    }
}
