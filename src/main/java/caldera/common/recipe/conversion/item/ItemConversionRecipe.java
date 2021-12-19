package caldera.common.recipe.conversion.item;

import caldera.common.recipe.conversion.ConversionRecipe;
import net.minecraft.world.item.ItemStack;

public interface ItemConversionRecipe extends ConversionRecipe<ItemStack> {

    ItemStack assemble(ItemStack input);
}
