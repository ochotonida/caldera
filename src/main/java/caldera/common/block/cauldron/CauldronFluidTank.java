package caldera.common.block.cauldron;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CauldronFluidTank extends FluidTank {

    private final CauldronBlockEntity cauldron;

    public CauldronFluidTank(CauldronBlockEntity cauldron) {
        super(FluidAttributes.BUCKET_VOLUME * 2);
        this.cauldron = cauldron;
    }

    @Override
    public int getCapacity() {
        // noinspection ConstantConditions
        if (cauldron.hasBrew()
                || !cauldron.itemHandler.isPresent()
                || !cauldron.itemHandler.orElse(null).getStackInSlot(0).isEmpty()
        ) {
            // only allow pumping fluids in & out if the cauldron does not contain a brew or items
            return 0;
        }
        return super.getCapacity();
    }
}
