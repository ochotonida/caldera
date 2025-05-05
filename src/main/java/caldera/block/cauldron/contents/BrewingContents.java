package caldera.block.cauldron.contents;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;

public class BrewingContents extends CauldronContentsWithInventory {

    public BrewingContents() {
        super(NonNullList.create());
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return EmptyFluidHandler.INSTANCE;
    }
}
