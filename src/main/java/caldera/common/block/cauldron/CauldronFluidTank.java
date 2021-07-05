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
        return !attributes.isLighterThanAir() && !attributes.isGaseous();
    }

    public void clear() {
        if (!isEmpty()) {
            cauldron.setFluidOrBrewChanged();
        }
        setFluid(FluidStack.EMPTY);
    }

    public boolean isFull() {
        return getSpace() <= 0;
    }

    @Override
    public void setFluid(FluidStack stack) {
        if (!stack.isFluidEqual(getFluid())) {
            cauldron.setFluidOrBrewChanged();
        }
        super.setFluid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (cauldron.canTransferFluids()) {
            FluidStack previousFluid = action == FluidAction.EXECUTE ? getFluid().copy() : null;
            int result = super.fill(resource, action);
            if (action == FluidAction.EXECUTE && !previousFluid.isFluidEqual(getFluid())) {
                cauldron.setFluidOrBrewChanged();
            }
            return result;
        }
        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (cauldron.canTransferFluids()) {
            FluidStack previousFluid = action == FluidAction.EXECUTE ? getFluid().copy() : null;
            FluidStack result = super.drain(maxDrain, action);
            if (action == FluidAction.EXECUTE && !previousFluid.isFluidEqual(getFluid())) {
                cauldron.setFluidOrBrewChanged();
            }
            return result;
        }
        return FluidStack.EMPTY;
    }

    @Override
    protected void onContentsChanged() {
        cauldron.sendUpdatePacket();
        cauldron.setChanged();
    }
}
