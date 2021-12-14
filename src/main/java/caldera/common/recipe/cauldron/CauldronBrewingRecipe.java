package caldera.common.recipe.cauldron;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public record CauldronBrewingRecipe(ResourceLocation id, boolean isOrdered, FluidIngredient fluidIngredient, NonNullList<Ingredient> ingredients, ResourceLocation result) implements OrderedCauldronRecipe<ResourceLocation> {

    @Override
    public ResourceLocation assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return result();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.CAULDRON_BREWING_SERIALIZER.get();
    }

    @Override
    public RecipeType<CauldronRecipe<ResourceLocation>> getType() {
        return ModRecipeTypes.CAULDRON_BREWING;
    }

    public static class Serializer extends OrderedCauldronRecipe.Serializer<CauldronBrewingRecipe, ResourceLocation> {

        @Override
        public CauldronBrewingRecipe createRecipe(ResourceLocation id, ResourceLocation result, boolean isOrdered, FluidIngredient fluidIngredient, NonNullList<Ingredient> ingredients) {
            return new CauldronBrewingRecipe(id, isOrdered, fluidIngredient, ingredients, result);
        }

        @Override
        public ResourceLocation readResult(JsonObject object) {
            return CraftingHelper.readResourceLocation(object, "result");
        }

        @Override
        public ResourceLocation readResult(FriendlyByteBuf buffer) {
            return buffer.readResourceLocation();
        }

        @Override
        public void writeResult(FriendlyByteBuf buffer, CauldronBrewingRecipe recipe) {
            buffer.writeResourceLocation(recipe.result);
        }
    }
}
