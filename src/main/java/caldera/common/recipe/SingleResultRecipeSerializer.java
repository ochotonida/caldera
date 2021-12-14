package caldera.common.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface SingleResultRecipeSerializer<RESULT, RECIPE extends Recipe<?>> extends RecipeSerializer<RECIPE> {

    RESULT readResult(JsonObject object);

    RESULT readResult(FriendlyByteBuf buffer);

    void writeResult(FriendlyByteBuf buffer, RECIPE recipe);
}
