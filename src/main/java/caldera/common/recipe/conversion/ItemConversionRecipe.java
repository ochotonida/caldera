package caldera.common.recipe.conversion;

import net.minecraft.world.item.ItemStack;

public interface ItemConversionRecipe extends ConversionRecipe<ItemStack> {

    ItemStack assemble(ItemStack input);
}
