package caldera.common.recipe;

import caldera.common.recipe.ingredient.FluidIngredient;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class OrderedCauldronRecipe<RESULT> implements CauldronRecipe<RESULT> {

    private final ResourceLocation id;
    private final boolean isOrdered;
    private final FluidIngredient fluidIngredient;
    private final NonNullList<Ingredient> ingredients;

    public OrderedCauldronRecipe(ResourceLocation id, boolean isOrdered, FluidIngredient fluidIngredient, List<Ingredient> ingredients) {
        this.id = id;
        this.isOrdered = isOrdered;
        this.fluidIngredient = fluidIngredient;
        this.ingredients = NonNullList.of(Ingredient.EMPTY, ingredients.toArray(new Ingredient[0]));
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public FluidIngredient getFluidIngredient() {
        return fluidIngredient;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity) {
        if (!getFluidIngredient().test(fluid)) {
            return false;
        }

        if (isOrdered()) {
            return matchesOrdered(inventory);
        } else {
            return matchesUnordered(inventory);
        }
    }

    public boolean matchesOrdered(IItemHandler inventory) {
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

    public boolean matchesUnordered(IItemHandler inventory) {
        List<ItemStack> inputs = new ArrayList<>();

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack itemstack = inventory.getStackInSlot(slot);
            if (!itemstack.isEmpty()) {
                inputs.add(itemstack);
            }
        }

        return RecipeMatcher.findMatches(inputs, getIngredients()) != null;
    }

    public static abstract class Serializer<RECIPE extends OrderedCauldronRecipe<RESULT>, RESULT>
            extends ForgeRegistryEntry<IRecipeSerializer<?>>
            implements IRecipeSerializer<RECIPE> {

        public abstract RECIPE createRecipe(
                ResourceLocation id,
                RESULT result,
                boolean isOrdered,
                FluidIngredient fluidIngredient,
                List<Ingredient> ingredients
        );

        public abstract RESULT readResult(JsonObject object);

        public abstract RESULT readResult(PacketBuffer buffer);

        public abstract void writeResult(PacketBuffer buffer, RECIPE recipe);

        @Override
        public RECIPE fromJson(ResourceLocation id, JsonObject object) {
            List<Ingredient> ingredients = CraftingHelper.getIngredients(object, "ingredients");
            FluidIngredient fluidIngredient = FluidIngredient.fromJson(object, "fluid");
            RESULT result = readResult(object);
            boolean isOrdered = JSONUtils.getAsBoolean(object, "ordered");

            return createRecipe(id, result, isOrdered, fluidIngredient, ingredients);
        }

        @Override
        public RECIPE fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            List<Ingredient> ingredients = CraftingHelper.readIngredientsFromBuffer(buffer);
            FluidIngredient fluidIngredient = FluidIngredient.fromBuffer(buffer);
            RESULT result = readResult(buffer);
            boolean isOrdered = buffer.readBoolean();

            return createRecipe(id, result, isOrdered, fluidIngredient, ingredients);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, RECIPE recipe) {
            CraftingHelper.writeIngredientsToBuffer(buffer, recipe.getIngredients());
            recipe.getFluidIngredient().toBuffer(buffer);
            writeResult(buffer, recipe);
            buffer.writeBoolean(recipe.isOrdered());
        }
    }
}
