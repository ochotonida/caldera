package caldera.common.block.cauldron;

import caldera.common.init.ModBlockEntityTypes;
import caldera.common.init.ModSoundEvents;
import caldera.common.init.ModTags;
import caldera.common.recipe.Brew;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
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

    private Vector3d getCauldronCenter() {
        CauldronBlockEntity controller = getController();

        if (controller == null) {
            return Vector3d.ZERO;
        }

        double floorHeight = 4 / 16D;
        BlockPos pos = controller.getBlockPos();
        return new Vector3d(pos.getX() + 1, pos.getY() + floorHeight, pos.getZ() + 1);
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
        if (hasBrew()) {
            getBrew().onEntityInside(entity, yOffset);
        } else if (!entity.level.isClientSide() && entity instanceof ItemEntity) {
            onItemInside((ItemEntity) entity, yOffset);
        }
    }

    private void onItemInside(ItemEntity itemEntity, double yOffset) {
        CompoundNBT itemData = itemEntity.getPersistentData();

        if (itemEntity.getDeltaMovement().y() <= 0
                && !itemData.contains("InitialDeltaMovement", Constants.NBT.TAG_COMPOUND)
        ) {
            CompoundNBT nbt = new CompoundNBT();
            Vector3d deltaMovement = itemEntity.getDeltaMovement();

            nbt.putDouble("X", deltaMovement.x());
            nbt.putDouble("Y", deltaMovement.y());
            nbt.putDouble("Z", deltaMovement.z());

            itemData.put("InitialDeltaMovement", nbt);
        }

        if (fluidTank.getSpace() <= 0
                && itemEntity.getDeltaMovement().y() <= 0
                && yOffset < 0.2
        ) {
            addItemToCauldron(itemEntity);
        }
    }

    private void addItemToCauldron(ItemEntity itemEntity) {
        if (getLevel() == null) {
            return;
        }

        CompoundNBT itemData = itemEntity.getPersistentData();

        Vector3d motion;
        if (itemData.contains("InitialDeltaMovement", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT nbt = itemData.getCompound("InitialDeltaMovement");
            motion = new Vector3d(
                    nbt.getDouble("X"),
                    nbt.getDouble("Y"),
                    nbt.getDouble("Z")
            );
        } else {
            motion = itemEntity.getDeltaMovement();
        }

        motion = motion
                .multiply(-1, 0, -1)
                .normalize()
                .scale(0.2)
                .add(0, 0.425, 0);

        ItemStack remainder = itemEntity.getItem();

        if (isValidIngredient(remainder)) {
            ItemStack stack = remainder.split(1);
            inventory.addItem(stack);

            if (stack.hasContainerItem()) {
                ItemStack containerStack = stack.getContainerItem();
                spawnInCauldron(containerStack, motion);
            }
        } else {
            getLevel().playSound(
                    null,
                    itemEntity.getX(),
                    itemEntity.getY(),
                    itemEntity.getZ(),
                    ModSoundEvents.CAULDRON_RETURN_INERT_INGREDIENT.get(),
                    SoundCategory.BLOCKS,
                    0.5F,
                    1
            );
        }

        spawnInCauldron(remainder, motion);

        getLevel().playSound(
                null,
                itemEntity.getX(),
                itemEntity.getY(),
                itemEntity.getZ(),
                SoundEvents.GENERIC_SPLASH,
                SoundCategory.BLOCKS,
                1,
                1
        );
        itemEntity.remove();
    }

    private boolean isValidIngredient(ItemStack stack) {
        return !ModTags.INERT.contains(stack.getItem());
    }

    private void spawnInCauldron(ItemStack stack, Vector3d motion) {
        if (getLevel() == null) {
            return;
        }

        Vector3d position = getCauldronCenter();

        ItemEntity itemEntity = new ItemEntity(getLevel(), position.x(), position.y(), position.z(), stack);
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setDeltaMovement(motion);
        getLevel().addFreshEntity(itemEntity);
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
                && (side == null || level != null && getBlockState().getValue(LargeCauldronBlock.HALF) == DoubleBlockHalf.LOWER)
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
        inventory.deserializeNBT(nbt.getCompound("ItemHandler"));
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundNBT()));
        nbt.put("ItemHandler", inventory.serializeNBT());
        return super.save(nbt);
    }
}
