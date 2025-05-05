package caldera.block.cauldron.contents;

import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class CauldronFluidInventory extends FluidTank {

    public static final int CAPACITY = 2000;

    public CauldronFluidInventory() {
        super(CAPACITY);
    }
}
