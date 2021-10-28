package caldera.common.block.cauldron;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CauldronFluidTank extends FluidTank {

    public static final int CAPACITY = FluidAttributes.BUCKET_VOLUME * 2;

    private final CauldronBlockEntity cauldron;

    public CauldronFluidTank(CauldronBlockEntity cauldron) {
        super(CAPACITY, CauldronFluidTank::isValid);
        this.cauldron = cauldron;
    }

    private static boolean isValid(FluidStack fluid) {
        // TODO replace this with a fluid tag maybe
        FluidAttributes attributes = fluid.getFluid().getAttributes();
        return !attributes.isLighterThanAir() || !attributes.isGaseous();
    }

    public void clear() {
        setFluid(FluidStack.EMPTY);
    }

    public boolean isFull() {
        return getSpace() <= 0;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (cauldron.canTransferFluids()) {
            return super.fill(resource, action);
        }
        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (cauldron.canTransferFluids()) {
            return super.drain(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    protected void onContentsChanged() {
        cauldron.sendBlockUpdated();
        cauldron.setChanged();
    }
}
