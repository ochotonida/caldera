package caldera.common.recipe.brew;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.CauldronRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public interface BrewType<BREW extends Brew> extends CauldronRecipe<BREW> {

    @Override
    default IRecipeType<?> getType() {
        return ModRecipeTypes.BREW_TYPE;
    }

    /**
     * Load a brew of this type from nbt
     *
     * @param nbt Compound tag to read the brew from
     * @param blockEntity The block entity loading the brew, might not have been fully initialized
     * @return A brew of this type
     */
    BREW loadBrew(CompoundNBT nbt, TileEntity blockEntity);

}
