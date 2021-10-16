package caldera.common.recipe.brew;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.Cauldron;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public interface BrewType<BREW extends Brew> extends Recipe<Container> {

    /**
     * Creates the result of this recipe
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items in the cauldron. These will be discarded after creating the result,
     * @param cauldron The cauldron constructing this recipe
     * @return The result of this recipe
     */
    BREW assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron);

    /**
     * Load a brew of this type from nbt
     *
     * @param nbt Compound tag to read the brew from
     * @param cauldron The block entity loading the brew, might not have been fully initialized
     * @return A brew of this type
     */
    BREW loadBrew(CompoundTag nbt, Cauldron cauldron);

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

    @Override
    default RecipeType<?> getType() {
        return ModRecipeTypes.BREW_TYPE;
    }

}
