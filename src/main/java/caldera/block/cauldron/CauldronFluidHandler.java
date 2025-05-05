package caldera.block.cauldron;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class CauldronFluidHandler implements IFluidHandler {

    private final Cauldron cauldron;

    public CauldronFluidHandler(Cauldron cauldron) {
        this.cauldron = cauldron;
    }

    @Override
    public int getTanks() {
        return cauldron.getContents().getFluidHandler().getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return cauldron.getContents().getFluidHandler().getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return cauldron.getContents().getFluidHandler().getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack fluidStack) {
        return cauldron.getContents().getFluidHandler().isFluidValid(tank, fluidStack);
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        return cauldron.getContents().getFluidHandler().fill(fluidStack, fluidAction);
    }

    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        return cauldron.getContents().getFluidHandler().drain(fluidStack, fluidAction);
    }

    @Override
    public FluidStack drain(int tank, FluidAction fluidAction) {
        return cauldron.getContents().getFluidHandler().drain(tank, fluidAction);
    }
}
