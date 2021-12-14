package caldera.common.brew;

import caldera.common.block.cauldron.Cauldron;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public interface BrewType {

    ResourceLocation getId();

    /**
     * Creates a brew of this brew type
     *
     * @param fluid The fluid in the cauldron. The size of this fluid stack is always equal to the maximum capacity
     *              (= 2 buckets) when called
     * @param inventory The items in the cauldron. These will be discarded after creating the result
     * @param cauldron The cauldron constructing this brew
     * @return A brew of this type
     */
    Brew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron);

    /**
     * Creates a brew of this type to be loaded from nbt
     *
     * @param cauldron The cauldron loading the brew
     * @return A brew of this type
     */
    Brew create(Cauldron cauldron);

    BrewTypeSerializer<?> getSerializer();
}
