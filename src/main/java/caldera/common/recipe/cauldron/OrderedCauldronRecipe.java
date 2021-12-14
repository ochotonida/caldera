package caldera.common.recipe.cauldron;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.recipe.SingleResultRecipeSerializer;
import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

public interface OrderedCauldronRecipe<RESULT> extends CauldronRecipe<RESULT> {

    ResourceLocation id();
    boolean isOrdered();
    FluidIngredient fluidIngredient();
    NonNullList<Ingredient> ingredients();


    @Override
    default ResourceLocation getId() {
        return id();
    }

    @Override
    default NonNullList<Ingredient> getIngredients() {
        return ingredients();
    }

    @Override
    default boolean matches(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        if (!fluidIngredient().test(fluid)) {
            return false;
        }

        if (isOrdered()) {
            return matchesOrdered(inventory);
        } else {
            return matchesUnordered(inventory);
        }
    }

    default boolean matchesOrdered(IItemHandler inventory) {
        int slot = -1;

        for (Ingredient ingredient : getIngredients()) {
            ItemStack stack;

            do {
                if (++slot >= inventory.getSlots()) {
                    return false;
                }
                stack = inventory.getStackInSlot(slot);
            } while (stack.isEmpty());

            if (!ingredient.test(stack)) {
                return false;
            }
        }

        while (++slot < inventory.getSlots()) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    default boolean matchesUnordered(IItemHandler inventory) {
        List<ItemStack> inputs = new ArrayList<>();

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack itemstack = inventory.getStackInSlot(slot);
            if (!itemstack.isEmpty()) {
                inputs.add(itemstack);
            }
        }

        return RecipeMatcher.findMatches(inputs, getIngredients()) != null;
    }

    abstract class Serializer<RECIPE extends OrderedCauldronRecipe<RESULT>, RESULT>
            extends ForgeRegistryEntry<RecipeSerializer<?>>
            implements SingleResultRecipeSerializer<RESULT, RECIPE> {

        public abstract RECIPE createRecipe(ResourceLocation id, RESULT result, boolean isOrdered, FluidIngredient fluidIngredient, NonNullList<Ingredient> ingredients);

        @Override
        public RECIPE fromJson(ResourceLocation id, JsonObject object) {
            List<Ingredient> ingredients = CraftingHelper.readIngredients(object, "ingredients");
            FluidIngredient fluidIngredient = FluidIngredient.fromJson(object, "fluid");
            RESULT result = readResult(object);
            boolean isOrdered = GsonHelper.getAsBoolean(object, "ordered");

            return createRecipe(id, result, isOrdered, fluidIngredient, toNonNullList(ingredients));
        }

        @Override
        public RECIPE fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<Ingredient> ingredients = CraftingHelper.readIngredients(buffer);
            FluidIngredient fluidIngredient = FluidIngredient.fromBuffer(buffer);
            RESULT result = readResult(buffer);
            boolean isOrdered = buffer.readBoolean();

            return createRecipe(id, result, isOrdered, fluidIngredient, toNonNullList(ingredients));
        }

        private static NonNullList<Ingredient> toNonNullList(List<Ingredient> ingredients) {
            return NonNullList.of(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0]));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RECIPE recipe) {
            CraftingHelper.writeIngredients(buffer, recipe.getIngredients());
            recipe.fluidIngredient().toBuffer(buffer);
            writeResult(buffer, recipe);
            buffer.writeBoolean(recipe.isOrdered());
        }
    }
}
