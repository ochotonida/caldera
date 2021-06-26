package caldera.common.block.cauldron;

import caldera.common.init.ModBlockEntityTypes;
import caldera.common.recipe.Brew;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;

public class CauldronBlockEntity extends TileEntity implements ITickableTileEntity  {

    protected LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> EmptyHandler.INSTANCE);
    protected LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> EmptyFluidHandler.INSTANCE);
    protected final CauldronFluidTank fluidTank;
    protected final CauldronItemHandler inventory;
    private Brew brew;

    public CauldronBlockEntity() {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get());
        fluidTank = new CauldronFluidTank(this);
        inventory = new CauldronItemHandler(this);
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
        return getLevel() != null && LargeCauldronBlock.isOrigin(getBlockState());
    }

    public Brew getBrew() {
        return brew;
    }

    public boolean hasBrew() {
        return getBrew() != null;
    }

    protected boolean canTransferFluids() {
        // only allow pumping fluids in & out if the cauldron does not contain a brew or items
        return !hasBrew() && inventory.isEmpty();
    }

    public double getFluidLevel() {
        if (hasBrew()) {
            return getBrew().getFluidLevel();
        } else if (!fluidTank.isEmpty()) {
            return fluidTank.getFluidAmount() / (double) fluidTank.getCapacity();
        } else {
            return 0;
        }
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

    protected void onEntityInside(Entity entity, double yOffset) {
        if (yOffset < getFluidLevel()) {
            if (!entity.level.isClientSide()
                    && !hasBrew()
                    && entity instanceof ItemEntity
            ) {
                addToCauldron((ItemEntity) entity);
            }
        } else if (hasBrew()) {
            getBrew().onEntityInside(entity, yOffset);
        }
    }

    private void addToCauldron(ItemEntity itemEntity) {
        // TODO add brew inert tag
        // TODO add item limit
        FluidStack fluid = fluidTank.getFluid();
        if (!fluid.isEmpty() && fluid.getAmount() == fluidTank.getCapacity()) {
            inventory.addItem(itemEntity.getItem());
        }
    }

    protected ActionResultType onUse(PlayerEntity player, Hand hand) {
        if (player.level.isClientSide()) {
            return ActionResultType.SUCCESS;
        }

        if (FluidUtil.interactWithFluidHandler(player, hand, fluidTank)) {
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
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
    public AxisAlignedBB getRenderBoundingBox() {
        if (isController()) {
            return new AxisAlignedBB(getBlockPos(), getBlockPos().offset(2, 2, 2));
        }
        return super.getRenderBoundingBox();
    }

    protected void sendUpdatePacket() {
        if (getLevel() != null && !getLevel().isClientSide()) {
            BlockState state = getBlockState();
            getLevel().sendBlockUpdated(getBlockPos(), state, state, Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    // update client on chunk load
    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        readUpdateTag(tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return createUpdateTag(super.getUpdateTag());
    }

    // sync client on block update
    @Override
    public void onDataPacket(NetworkManager networkManager, SUpdateTileEntityPacket updatePacket) {
        if (level != null) {
            readUpdateTag(updatePacket.getTag());
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getBlockPos(), 0, createUpdateTag(new CompoundNBT()));
    }

    // load data sent to client
    protected void readUpdateTag(CompoundNBT nbt) {
        fluidTank.readFromNBT(nbt.getCompound("FluidHandler"));
    }

    protected CompoundNBT createUpdateTag(CompoundNBT nbt) {
        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundNBT()));
        return nbt;
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
