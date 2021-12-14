package caldera.common.recipe.transmutation;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ItemResultRecipeSerializer;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.CraftingHelper;

public record ItemTransmutationRecipe(ResourceLocation id, ResourceLocation transmutationType, Ingredient ingredient, ItemStack result) implements TransmutationRecipe<ItemStack, ItemStack, Ingredient> {

    @Override
    public ItemStack assemble(ResourceLocation transmutationType, ItemStack stack) {
        return result().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ITEM_TRANSMUTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_TRANSMUTATION;
    }

    public static class Serializer extends TransmutationRecipe.Serializer<ItemStack, ItemStack, Ingredient, ItemTransmutationRecipe> implements ItemResultRecipeSerializer<ItemTransmutationRecipe> {

        @Override
        public ItemTransmutationRecipe createRecipe(ResourceLocation id, ResourceLocation transmutationType, Ingredient ingredient, ItemStack result) {
            return new ItemTransmutationRecipe(id, transmutationType, ingredient, result);
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
