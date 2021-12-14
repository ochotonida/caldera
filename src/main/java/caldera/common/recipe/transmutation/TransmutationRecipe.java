package caldera.common.recipe.transmutation;

import caldera.common.recipe.ItemResultRecipe;
import caldera.common.recipe.SingleResultRecipeSerializer;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Predicate;

public interface TransmutationRecipe<RESULT, INPUT, INGREDIENT extends Predicate<INPUT>> extends ItemResultRecipe {

    ResourceLocation id();

    ResourceLocation transmutationType();

    INGREDIENT ingredient();

    default boolean matches(ResourceLocation transmutationType, INPUT input) {
        return transmutationType().equals(transmutationType) && ingredient().test(input);
    }

    RESULT assemble(ResourceLocation transmutationType, INPUT input);

    @Override
    default ResourceLocation getId() {
        return id();
    }

    // unused
    @Override
    @Deprecated
    default boolean matches(Container inventory, Level level) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack assemble(Container inventory) {
        return ItemStack.EMPTY;
    }

    // unused
    @Override
    @Deprecated
    default boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    abstract class Serializer<RESULT, INPUT, INGREDIENT extends Predicate<INPUT>, RECIPE extends TransmutationRecipe<RESULT, INPUT, INGREDIENT>>
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
