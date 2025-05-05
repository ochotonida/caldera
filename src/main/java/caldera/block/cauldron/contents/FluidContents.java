package caldera.block.cauldron.contents;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidContents extends CauldronContentsWithInventory {
    
    private final CauldronFluidInventory fluidInventory;

    public FluidContents() {
        this(NonNullList.create(), FluidStack.EMPTY);
    }

    public FluidContents(NonNullList<ItemStack> items, FluidStack stack) {
        super(items);
        this.fluidInventory = new CauldronFluidInventory();
        this.fluidInventory.setFluid(stack);
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidInventory;
    }
}
