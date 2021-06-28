package caldera.common.block.cauldron;

import caldera.Caldera;
import caldera.common.init.ModBlockEntityTypes;
import caldera.common.init.ModRecipeTypes;
import caldera.common.init.ModSoundEvents;
import caldera.common.init.ModTags;
import caldera.common.recipe.CauldronRecipe;
import caldera.common.recipe.brew.Brew;
import caldera.common.recipe.brew.BrewType;
import caldera.common.util.RecipeHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;
import java.util.Collection;

public class CauldronBlockEntity extends TileEntity implements ITickableTileEntity  {

    private static final ResourceLocation SLUDGE_TYPE = new ResourceLocation(Caldera.MODID, "brew_types/sludge");

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

            if (stack.hasContainerItem()) {
                ItemStack containerStack = stack.getContainerItem();
                spawnInCauldron(containerStack, motion);
            }

            inventory.addItem(stack);
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

    protected void onIngredientsUpdated() {
        if (getLevel() == null) {
            return;
        }

        RecipeManager manager = getLevel().getRecipeManager();

        CauldronRecipe<ItemStack> itemRecipe = findMatchingRecipe(manager, ModRecipeTypes.CAULDRON_ITEM_CRAFTING);

        if (itemRecipe != null) {
            craftItem(itemRecipe);
            return;
        }

        CauldronRecipe<FluidStack> fluidRecipe = findMatchingRecipe(manager, ModRecipeTypes.CAULDRON_FLUID_CRAFTING);

        if (fluidRecipe != null) {
            craftFluid(fluidRecipe);
            return;
        }

        BrewType<?> brewType = findMatchingRecipe(manager, ModRecipeTypes.BREW_TYPE);

        if (brewType != null) {
            createBrew(brewType);
            return;
        }

        if (inventory.isFull()) {
            setBrewToSludge();
        }
    }

    @Nullable
    private <RECIPE extends CauldronRecipe<?>> RECIPE findMatchingRecipe(RecipeManager manager, IRecipeType<RECIPE> type) {
        Collection<RECIPE> itemRecipes = RecipeHelper.byType(manager, type).values();
        for (RECIPE recipe : itemRecipes) {
            if (matchesRecipe(recipe)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean matchesRecipe(CauldronRecipe<?> recipe) {
        return recipe.matches(fluidTank.getFluid(), inventory, this);
    }

    private <RESULT> RESULT assembleRecipe(CauldronRecipe<RESULT> recipe) {
        return recipe.assemble(fluidTank.getFluid(), inventory, this);
    }

    private void craftItem(CauldronRecipe<ItemStack> recipe) {
        ItemStack result = assembleRecipe(recipe);
        spawnInCauldron(result, new Vector3d(0, 0.5, 0));

        inventory.clear();
        fluidTank.clear();
        sendUpdatePacket();
        setChanged();
    }

    private void craftFluid(CauldronRecipe<FluidStack> recipe) {
        FluidStack result = assembleRecipe(recipe);

        inventory.clear();
        fluidTank.setFluid(result);
        sendUpdatePacket();
        setChanged();
    }

    protected void setBrewToSludge() {
        BrewType<?> brewType = RecipeHelper.byType(ModRecipeTypes.BREW_TYPE).get(SLUDGE_TYPE);

        if (brewType == null) {
            Caldera.LOGGER.error("Failed to load brew type {} for cauldron at {}", SLUDGE_TYPE.toString(), getBlockPos());
            return;
        }

        createBrew(brewType);
    }

    protected void createBrew(BrewType<?> brewType) {
        brew = assembleRecipe(brewType);

        inventory.clear();
        fluidTank.clear();
        sendUpdatePacket();
        setChanged();
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
        readUpdateTag(updatePacket.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getBlockPos(), 0, createUpdateTag(new CompoundNBT()));
    }

    // load data sent to client
    protected void readUpdateTag(CompoundNBT nbt) {
        fluidTank.readFromNBT(nbt.getCompound("FluidHandler"));
        loadBrew(nbt);
    }

    protected CompoundNBT createUpdateTag(CompoundNBT nbt) {
        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundNBT()));
        saveBrew(nbt);

        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        fluidTank.readFromNBT(nbt.getCompound("FluidHandler"));
        inventory.deserializeNBT(nbt.getCompound("ItemHandler"));
        loadBrew(nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundNBT()));
        nbt.put("ItemHandler", inventory.serializeNBT());
        saveBrew(nbt);

        return super.save(nbt);
    }

    private void loadBrew(CompoundNBT nbt) {
        brew = null;

        if (nbt.contains("Brew", Constants.NBT.TAG_COMPOUND)) {
            String rawBrewTypeId = nbt.getString("BrewType");
            ResourceLocation brewTypeId = ResourceLocation.tryParse(rawBrewTypeId);

            if (brewTypeId == null) {
                Caldera.LOGGER.error("The cauldron at {} has invalid brew type {}. " +
                        "The brew will be discarded.", getBlockPos(), rawBrewTypeId
                );
                return;
            }

            BrewType<?> brewType = RecipeHelper.byType(ModRecipeTypes.BREW_TYPE).get(brewTypeId);
            if (brewType == null) {
                Caldera.LOGGER.error("The cauldron at {} has brew type {} which no longer exists. " +
                        "The brew will be discarded.", getBlockPos(), rawBrewTypeId
                );
                return;
            }

            brew = brewType.loadBrew(nbt.getCompound("Brew"), this);
        }
    }

    private void saveBrew(CompoundNBT nbt) {
        if (getLevel() == null) {
            throw new IllegalArgumentException();
        }

        if (hasBrew()) {
            CompoundNBT brewNBT = new CompoundNBT();
            brew.writeBrew(brewNBT);
            nbt.put("Brew", brewNBT);
            nbt.putString("BrewType", brew.getType().getId().toString());
        }
    }
}
