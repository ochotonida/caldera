package caldera.common.recipe;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public interface CauldronRecipe<RESULT> extends IRecipe<IInventory> {

    /**
     * Check whether the cauldron's current contents match this recipe
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items currently in the cauldron (maximum capacity = 8)
     * @param blockEntity The block entity trying to construct the recipe
     * @return Whether the cauldron's current contents match this recipe
     */
    boolean matches(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity);

    /**
     * Creates the result of this recipe
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items in the cauldron. These will be discarded after creating the result,
     * @param blockEntity The cauldron constructing this recipe
     * @return The result of this recipe
     */
    RESULT assemble(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity);

    // unused
    @Override
    @Deprecated
    default boolean matches(IInventory inventory, World level) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    default ItemStack assemble(IInventory inventory) {
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
