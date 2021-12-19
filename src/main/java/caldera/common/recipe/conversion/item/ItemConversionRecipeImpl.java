package caldera.common.recipe.conversion.item;

import caldera.common.init.ModRecipeTypes;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public record ItemConversionRecipeImpl(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result) implements ItemConversionRecipe {

    @Override
    public boolean matches(ItemStack input) {
        return ingredient.test(input);
    }

    @Override
    public ItemStack assemble(ItemStack input) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ITEM_CONVERSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_CONVERSION;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ItemConversionRecipeImpl> {

        @Override
        public ItemConversionRecipeImpl fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            Ingredient ingredient = CraftingHelper.readIngredient(object, "ingredient");
            ItemStack result = CraftingHelper.readItemStack(object, "result", true);
            if (result.getCount() != 1) {
                throw new JsonParseException("Item conversion result should have a count of 1");
            }
            return new ItemConversionRecipeImpl(id, conversionType, ingredient, result);
        }

        @Nullable
        @Override
        public ItemConversionRecipeImpl fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();

            return new ItemConversionRecipeImpl(id, conversionType, ingredient, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ItemConversionRecipeImpl recipe) {
            buffer.writeResourceLocation(recipe.conversionType);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }
}
