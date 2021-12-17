package caldera.common.recipe.cauldron;

import caldera.common.block.cauldron.Cauldron;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public interface CauldronRecipe<RESULT> extends Recipe<Container> {

    /**
     * Check whether the cauldron's current contents match this recipe
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items currently in the cauldron (maximum capacity = 8)
     * @param cauldron The cauldron trying to construct the recipe
     * @return Whether the cauldron's current contents match this recipe
     */
    boolean matches(FluidStack fluid, IItemHandler inventory, Cauldron cauldron);

    /**
     * Creates the result of this recipe
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items in the cauldron. These will be discarded after creating the result,
     * @param cauldron The cauldron constructing this recipe
     * @return The result of this recipe
     */
    RESULT assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron);

    @Override
    default boolean isSpecial() {
        return true;
    }

    // unused
    @Override
    @Deprecated
    default boolean matches(Container inventory, Level level) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack assemble(Container inventory) {
        return ItemStack.EMPTY;
    }

    // unused
    @Override
    @Deprecated
    default boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
