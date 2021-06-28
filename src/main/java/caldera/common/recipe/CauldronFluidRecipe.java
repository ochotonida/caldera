package caldera.common.recipe;

import caldera.common.block.cauldron.CauldronFluidTank;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
    public FluidStack assemble(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity) {
        return result.copy();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_FLUID_CRAFTING_SERIALIZER.get();
    }

    @Override
    public IRecipeType<CauldronRecipe<FluidStack>> getType() {
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
                        String.format("Fluid amount must be smaller than %s mB, is %s",
                                CauldronFluidTank.CAPACITY,
                                result.getAmount()
                        )
                );
            }

            return result;
        }

        @Override
        public FluidStack readResult(PacketBuffer buffer) {
            return FluidStack.readFromPacket(buffer);
        }

        @Override
        public void writeResult(PacketBuffer buffer, CauldronFluidRecipe recipe) {
            recipe.result.writeToPacket(buffer);
        }
    }
}
