package caldera.common.recipe;

import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface ItemResultRecipeSerializer<RECIPE extends ItemResultRecipe> extends SingleResultRecipeSerializer<ItemStack, RECIPE> {

    @Override
    default ItemStack readResult(JsonObject object) {
        return CraftingHelper.readItemStack(object, "result", true);
    }

    @Override
    default ItemStack readResult(FriendlyByteBuf buffer) {
        return buffer.readItem();
    }

    @Override
    default void writeResult(FriendlyByteBuf buffer, RECIPE recipe) {
        buffer.writeItem(recipe.result());
    }
}
