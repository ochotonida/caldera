package caldera.common.recipe;

import caldera.common.block.cauldron.CauldronFluidTank;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class CauldronFluidRecipe extends OrderedCauldronRecipe<FluidStack> {

    private final FluidStack result;

    public CauldronFluidRecipe(ResourceLocation id, FluidStack result, boolean isOrdered, FluidIngredient fluidIngredient, List<Ingredient> ingredients) {
        super(id, isOrdered, fluidIngredient, ingredients);
        this.result = result;
    }

    @Override
    public FluidStack assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_FLUID_CRAFTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CauldronRecipe<FluidStack>> getType() {
        return ModRecipeTypes.CAULDRON_FLUID_CRAFTING;
    }

    public static class Serializer extends OrderedCauldronRecipe.Serializer<CauldronFluidRecipe, FluidStack> {

        @Override
        public CauldronFluidRecipe createRecipe(
                ResourceLocation id,
                FluidStack result,
                boolean isOrdered,
                FluidIngredient fluidIngredient,
                List<Ingredient> ingredients
        ) {
            return new CauldronFluidRecipe(id, result, isOrdered, fluidIngredient, ingredients);
        }

        @Override
        public FluidStack readResult(JsonObject object) {
            FluidStack result = CraftingHelper.readFluidStack(object, "result", true, true);

            if (result.getAmount() > CauldronFluidTank.CAPACITY) {
                throw new JsonParseException(
                        "Fluid amount must be smaller than %s mB, is %s".formatted(CauldronFluidTank.CAPACITY, result.getAmount())
                );
            }

            return result;
        }

        @Override
        public FluidStack readResult(FriendlyByteBuf buffer) {
            return FluidStack.readFromPacket(buffer);
        }

        @Override
        public void writeResult(FriendlyByteBuf buffer, CauldronFluidRecipe recipe) {
            recipe.result.writeToPacket(buffer);
        }
    }
}
