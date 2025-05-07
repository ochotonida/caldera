package caldera.block.cauldron.contents;

import caldera.registry.ModCauldronContentTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidContents extends CauldronContentsWithInventory {

    public static final MapCodec<FluidContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CauldronItemInventory.CODEC.fieldOf("items").forGetter(FluidContents::getItemHandler),
            CauldronFluidInventory.CODEC.fieldOf("fluid").forGetter(FluidContents::getFluidHandler)
    ).apply(instance, FluidContents::new));

    private final CauldronFluidInventory fluidInventory;

    public FluidContents() {
        this(NonNullList.withSize(CauldronItemInventory.DEFAULT_INVENTORY_SIZE, ItemStack.EMPTY), FluidStack.EMPTY);
    }

    public FluidContents(NonNullList<ItemStack> items, FluidStack stack) {
        super(items);
        this.fluidInventory = new CauldronFluidInventory();
        this.fluidInventory.setFluid(stack);
    }

    public FluidContents(CauldronItemInventory itemInventory, CauldronFluidInventory fluidInventory) {
        super(itemInventory);
        this.fluidInventory = fluidInventory;
    }

    @Override
    public Type<?> getType() {
        return ModCauldronContentTypes.FLUID.get();
    }

    @Override
    public CauldronFluidInventory getFluidHandler() {
        return fluidInventory;
    }
}
