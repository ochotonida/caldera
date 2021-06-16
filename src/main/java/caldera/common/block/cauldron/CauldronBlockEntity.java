package caldera.common.block.cauldron;

import caldera.common.init.ModBlockEntityTypes;
import caldera.common.recipe.Brew;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;

public class CauldronBlockEntity extends TileEntity implements ITickableTileEntity  {

    protected LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> EmptyHandler.INSTANCE);
    protected LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> EmptyFluidHandler.INSTANCE);
    private final CauldronFluidTank fluidTank;
    private final CauldronItemHandler inventory;
    private Brew brew;

    public CauldronBlockEntity() {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get());
        fluidTank = new CauldronFluidTank(this);
        inventory = new CauldronItemHandler();
    }

    @Nullable
    public CauldronBlockEntity getController() {
        if (isController()) {
            return this;
        }
        if (getLevel() != null) {
            return LargeCauldronBlock.getController(getBlockState(), getBlockPos(), getLevel());
        }
        return null;
    }

    public boolean isController() {
        return getLevel() != null && getBlockState().getValue(LargeCauldronBlock.FACING) == Direction.SOUTH
                && getBlockState().getValue(LargeCauldronBlock.HALF) == DoubleBlockHalf.LOWER;
    }

    public Brew getBrew() {
        return brew;
    }

    public boolean hasBrew() {
        return getBrew() != null;
    }

    private void setupCapabilities() {
        LazyOptional<IItemHandler> oldItemHandler = itemHandler;
        LazyOptional<IFluidHandler> oldFluidHandler = fluidHandler;

        CauldronBlockEntity controller = getController();
        if (controller != null) {
            itemHandler = LazyOptional.of(() -> controller.inventory);
            fluidHandler = LazyOptional.of(() -> controller.fluidTank);
        } else {
            itemHandler = LazyOptional.of(() -> EmptyHandler.INSTANCE);
            fluidHandler = LazyOptional.of(() -> EmptyFluidHandler.INSTANCE);
        }

        oldItemHandler.invalidate();
        oldFluidHandler.invalidate();
    }

    @Override
    public void tick() {
        if (!fluidHandler.isPresent()) {
            setupCapabilities();
        }
    }

    protected void onEntityInside(Entity entity) {
        if (!entity.level.isClientSide()
                && !hasBrew()
                && entity instanceof ItemEntity
        ) {
            addToCauldron((ItemEntity) entity);
        }
    }

    private void addToCauldron(ItemEntity itemEntity) {
        // TODO add brew inert tag
        // TODO add item limit
        fluidHandler.ifPresent(fluidTank -> {
            FluidStack fluidStack = fluidTank.getFluidInTank(0);
            if (!fluidStack.isEmpty() && fluidStack.getAmount() >= fluidTank.getTankCapacity(0)) {
                itemHandler.ifPresent(itemHandler -> {
                    if (itemHandler instanceof CauldronItemHandler) {
                        ((CauldronItemHandler) itemHandler).addItem(itemEntity.getItem());
                    }
                });
            }
        });
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                && (side == null
                        || level != null
                        && getBlockState().getValue(LargeCauldronBlock.HALF) == DoubleBlockHalf.LOWER
                )
        ) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        fluidTank.readFromNBT(nbt.getCompound("FluidHandler"));
        inventory.readFromNBT(nbt.getList("ItemHandler", Constants.NBT.TAG_COMPOUND));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundNBT()));
        nbt.put("ItemHandler", inventory.writeToNBT());
        return super.save(nbt);
    }
}
