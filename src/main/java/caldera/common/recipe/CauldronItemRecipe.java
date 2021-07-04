package caldera.common.recipe;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class CauldronItemRecipe extends OrderedCauldronRecipe<ItemStack> {

    private final ItemStack result;

    public CauldronItemRecipe(ResourceLocation id, ItemStack result, boolean isOrdered, FluidIngredient fluidIngredient, List<Ingredient> ingredients) {
        super(id, isOrdered, fluidIngredient, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return result.copy();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_ITEM_CRAFTING_SERIALIZER.get();
    }

    @Override
    public IRecipeType<CauldronRecipe<ItemStack>> getType() {
        return ModRecipeTypes.CAULDRON_ITEM_CRAFTING;
    }

    public static class Serializer extends OrderedCauldronRecipe.Serializer<CauldronItemRecipe, ItemStack> {

        @Override
        public CauldronItemRecipe createRecipe(
                ResourceLocation id,
                ItemStack result,
                boolean isOrdered,
                FluidIngredient fluidIngredient,
                List<Ingredient> ingredients
        ) {
            return new CauldronItemRecipe(id, result, isOrdered, fluidIngredient, ingredients);
        }

        @Override
        public ItemStack readResult(JsonObject object) {
            return CraftingHelper.readItemStack(object, "result", true);
        }

        @Override
        public ItemStack readResult(PacketBuffer buffer) {
            return buffer.readItem();
        }

        @Override
        public void writeResult(PacketBuffer buffer, CauldronItemRecipe recipe) {
            buffer.writeItem(recipe.result);
        }
    }
}
