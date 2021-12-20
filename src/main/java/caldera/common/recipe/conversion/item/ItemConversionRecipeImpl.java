package caldera.common.recipe.conversion.item;

import caldera.common.init.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ItemConversionRecipeImpl extends AbstractItemConversionRecipe {

    public ItemConversionRecipeImpl(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result, boolean shouldKeepNbt) {
        super(id, conversionType, ingredient, result, shouldKeepNbt);
    }

    @Override
    public ItemStack assemble(ItemStack input) {
        ItemStack result = this.result.copy();
        if (shouldKeepNbt && input.getTag() != null) {
            result.getOrCreateTag().merge(input.getTag());
        }
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ITEM_CONVERSION_SERIALIZER.get();
    }
}
