package caldera.common.recipe;

import caldera.common.init.ModRecipeTypes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public abstract class BrewType<T extends Brew> implements IRecipe<IInventory> {

    private final ResourceLocation id;

    public BrewType(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public final ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeType<?> getType() {
        return ModRecipeTypes.BREW;
    }

    /**
     * Check whether a brew of this type can be created using the cauldron's current contents
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items currently in the cauldron (maximum capacity = 8)
     * @param blockEntity The block entity creating the brew
     * @return A brew of this type
     */
    public abstract boolean matches(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity);

    /**
     * Creates a new brew of this type
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items in the cauldron. These will be cleared after creating the brew
     * @param blockEntity The block entity creating the brew
     * @return A brew of this type
     */
    public abstract T createBrew(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity);

    /**
     * Load a brew of this type
     *
     * @param nbt Compound tag to read the brew from
     * @param blockEntity The block entity loading the brew, might not have been fully initialized
     * @return A brew of this type
     */
    public abstract T loadBrew(CompoundNBT nbt, TileEntity blockEntity);

    // unused
    @Override
    @Deprecated
    public final boolean matches(IInventory inventory, World level) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    public final ItemStack assemble(IInventory inventory) {
        return ItemStack.EMPTY;
    }

    // unused
    @Override
    @Deprecated
    public final boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // unused
    @Override
    @Deprecated
    public final ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }
}
