package caldera.common.recipe.conversion;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ItemResultRecipe;
import caldera.common.recipe.ItemResultRecipeSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;

public record ItemConversionRecipe(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result) implements ItemResultRecipe, IngredientConversionRecipe<ItemStack, ItemStack, Ingredient> {

    @Override
    public ItemStack assemble(ItemStack stack) {
        return result().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ITEM_CONVERSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_CONVERSION;
    }

    public static class Serializer extends IngredientConversionRecipe.Serializer<ItemStack, Ingredient, ItemConversionRecipe> implements ItemResultRecipeSerializer<ItemConversionRecipe> {

        @Override
        public ItemConversionRecipe createRecipe(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result) {
            return new ItemConversionRecipe(id, conversionType, ingredient, result);
        }

        @Override
        public ItemStack readResult(JsonObject object) {
            ItemStack result = ItemResultRecipeSerializer.super.readResult(object);
            if (result.getCount() != 1) {
                throw new JsonParseException("Item conversion result should have a count of 1");
            }
            return result;
        }

        @Override
        public Ingredient readIngredient(JsonElement element) {
            return CraftingHelper.getIngredient(element);
        }

        @Override
        public Ingredient readIngredient(FriendlyByteBuf buffer) {
            return Ingredient.fromNetwork(buffer);
        }

        @Override
        public void writeIngredient(Ingredient ingredient, FriendlyByteBuf buffer) {
            ingredient.toNetwork(buffer);
        }
    }
}
