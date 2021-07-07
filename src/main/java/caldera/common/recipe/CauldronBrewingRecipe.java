package caldera.common.recipe;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class CauldronBrewingRecipe extends OrderedCauldronRecipe<ResourceLocation> {

    private final ResourceLocation result;

    public CauldronBrewingRecipe(ResourceLocation id, ResourceLocation result, boolean isOrdered, FluidIngredient fluidIngredient, List<Ingredient> ingredients) {
        super(id, isOrdered, fluidIngredient, ingredients);
        this.result = result;
    }

    @Override
    public ResourceLocation assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return result;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_BREWING_SERIALIZER.get();
    }

    @Override
    public IRecipeType<CauldronRecipe<ResourceLocation>> getType() {
        return ModRecipeTypes.CAULDRON_BREWING;
    }

    public static class Serializer extends OrderedCauldronRecipe.Serializer<CauldronBrewingRecipe, ResourceLocation> {

        @Override
        public CauldronBrewingRecipe createRecipe(
                ResourceLocation id,
                ResourceLocation result,
                boolean isOrdered,
                FluidIngredient fluidIngredient,
                List<Ingredient> ingredients
        ) {
            return new CauldronBrewingRecipe(id, result, isOrdered, fluidIngredient, ingredients);
        }

        @Override
        public ResourceLocation readResult(JsonObject object) {
            return CraftingHelper.readResourceLocation(object, "result");
        }

        @Override
        public ResourceLocation readResult(PacketBuffer buffer) {
            return buffer.readResourceLocation();
        }

        @Override
        public void writeResult(PacketBuffer buffer, CauldronBrewingRecipe recipe) {
            buffer.writeResourceLocation(recipe.result);
        }
    }
}
