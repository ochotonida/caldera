package caldera.common.recipe.conversion;

import caldera.common.init.ModRecipeTypes;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ToolConversionRecipe(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result, boolean copyEnchantments) implements IngredientConversionRecipe<ItemStack, ItemStack, Ingredient> {

    @Override
    public ItemStack assemble(ResourceLocation transmutationType, ItemStack stack) {
        ItemStack result = this.result.copy();
        double inputDamage = stack.getDamageValue() / (double) stack.getMaxDamage();
        result.setDamageValue(Math.min(result.getMaxDamage() - 1, (int) (result.getMaxDamage() * inputDamage)));
        if (copyEnchantments) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            EnchantmentHelper.setEnchantments(enchantments, result);
        }
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.TOOL_CONVERSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_CONVERSION;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<ToolConversionRecipe> {

        @Override
        public ToolConversionRecipe fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            if (!object.has("ingredient")) {
                throw new JsonParseException("Missing ingredient");
            }
            Ingredient ingredient = Ingredient.fromJson(object.get("ingredient"));
            boolean copyEnchantments = GsonHelper.getAsBoolean(object, "copyEnchantments");
            ItemStack result = CraftingHelper.readItemStack(object, "result", true);
            if (result.getCount() != 1) {
                throw new JsonParseException("Item conversion result should have a count of 1");
            }
            return new ToolConversionRecipe(id, conversionType, ingredient, result, copyEnchantments);
        }

        @Nullable
        @Override
        public ToolConversionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            boolean copyNbt = buffer.readBoolean();
            ItemStack result = buffer.readItem();
            return new ToolConversionRecipe(id, conversionType, ingredient, result, copyNbt);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolConversionRecipe recipe) {
            buffer.writeResourceLocation(recipe.conversionType);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeBoolean(recipe.copyEnchantments);
            buffer.writeItem(recipe.result);
        }
    }
}
