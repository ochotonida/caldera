package caldera.common.recipe.cauldron;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.recipe.CustomRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public interface CauldronRecipe<RESULT> extends CustomRecipe {

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

}
