package caldera.block.cauldron.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class CauldronFluidInventory extends FluidTank {

    public static final int DEFAULT_CAPACITY = 2000;

    public static final Codec<CauldronFluidInventory> CODEC = codec(DEFAULT_CAPACITY, CauldronFluidInventory::isValid);

    public CauldronFluidInventory() {
        this(DEFAULT_CAPACITY, CauldronFluidInventory::isValid);
    }

    public CauldronFluidInventory(int capacity, Predicate<FluidStack> predicate) {
        super(capacity, predicate);
    }

    public static boolean isValid(FluidStack stack) {
        return true;
    }

    public static CauldronFluidInventory withFluid(int capacity, Predicate<FluidStack> predicate, FluidStack stack) {
        CauldronFluidInventory fluidInventory = new CauldronFluidInventory(capacity, predicate);
        fluidInventory.setFluid(stack);
        return fluidInventory;
    }

    public static Codec<CauldronFluidInventory> codec(int capacity, Predicate<FluidStack> predicate) {
        return FluidStack.OPTIONAL_CODEC
                .validate(fluidStack -> {
                    if (!fluidStack.isEmpty() && fluidStack.getAmount() > capacity) {
                        return DataResult.error(() -> "Fluid tank with capacity %s cannot hold fluid [%s]".formatted(capacity, fluidStack));
                    } else if (!predicate.test(fluidStack)) {
                        return DataResult.error(() -> "Invalid fluid for fluid tank: [%s]".formatted(fluidStack));
                    }
                    return DataResult.success(fluidStack);
                })
                .xmap(
                        fluidStack -> CauldronFluidInventory.withFluid(capacity, predicate, fluidStack),
                        FluidTank::getFluid
                );
    }
}
