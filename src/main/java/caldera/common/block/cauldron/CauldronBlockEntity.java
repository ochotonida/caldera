package caldera.common.block.cauldron;

import caldera.Caldera;
import caldera.common.init.*;
import caldera.common.recipe.Cauldron;
import caldera.common.recipe.CauldronRecipe;
import caldera.common.recipe.brew.Brew;
import caldera.common.recipe.brew.BrewType;
import caldera.common.util.ColorHelper;
import caldera.common.util.RecipeHelper;
import caldera.common.util.rendering.InterpolatedChasingValue;
import caldera.common.util.rendering.InterpolatedLinearChasingValue;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
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

public class CauldronBlockEntity extends TileEntity implements Cauldron, ITickableTileEntity  {

    private static final ResourceLocation SLUDGE_TYPE = new ResourceLocation(Caldera.MODID, "brew_types/sludge");
    private static final int BREW_TIME = 60;

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> EmptyHandler.INSTANCE);
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> EmptyFluidHandler.INSTANCE);
    protected final CauldronFluidTank fluidTank;
    protected final CauldronItemHandler inventory;
    private Brew brew;
    private int brewTimeRemaining;

    // Exclusively used for rendering
    protected static final int BREWING_COLOR = 0xA3C740;

    protected final InterpolatedChasingValue fluidLevel;
    protected final InterpolatedLinearChasingValue previousFluidAlpha;
    protected final InterpolatedLinearChasingValue fluidAlpha;
    protected final InterpolatedLinearChasingValue brewingColorAlpha;
    private boolean fluidOrBrewChanged;
    private FluidStack previousFluid;
    private Brew previousBrew;

    public CauldronBlockEntity() {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get());
        fluidTank = new CauldronFluidTank(this);
        inventory = new CauldronItemHandler(this);

        fluidLevel = new InterpolatedChasingValue().withSpeed(1/2F).start(0);
        previousFluidAlpha = new InterpolatedLinearChasingValue().withStep(1/20F).start(0);
        fluidAlpha = new InterpolatedLinearChasingValue().withStep(1/20F).start(1);

        brewingColorAlpha = new InterpolatedLinearChasingValue().withStep(1/30F).start(0);

        previousFluid = FluidStack.EMPTY;
        previousBrew = null;
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

    public Vector3d getCenter() {
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

    public FluidStack getFluid() {
        return fluidTank.getFluid();
    }

    public Brew getPreviousBrew() {
        return previousBrew;
    }

    public FluidStack getPreviousFluid() {
        return previousFluid;
    }

    public boolean hasBrew() {
        return getBrew() != null;
    }

    public boolean hasFluid() {
        return !getFluid().isEmpty();
    }

    public boolean isEmpty() {
        return !hasBrew() && !hasFluid();
    }

    protected boolean canTransferFluids() {
        // only allow pumping fluids in & out if the cauldron does not contain a brew or items
        return !hasBrew() && inventory.isEmpty();
    }

    public double getFluidLevel() {
        if (hasBrew()) {
            return getBrew().getFluidLevel();
        } else if (hasFluid()) {
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

        if (isController()) {
            tickController();
        }
    }

    public void tickController() {
        if (getLevel() == null) {
            return;
        }

        updateBrewing();

        if (getLevel().isClientSide()) {
            if (fluidTank.isFull()) {
                spawnParticles(ModParticleTypes.CAULDRON_BUBBLE.get(), 2, getFluidParticleColor());
            }

            fluidLevel.tick();
            fluidAlpha.tick();
            if (Math.abs(fluidAlpha.getTarget() - fluidAlpha.value) < 1 / 2F) {
                previousFluidAlpha.tick();
            }
            brewingColorAlpha.tick();
        }
    }

    private void updateBrewing() {
        if (brewTimeRemaining-- > 0 && getLevel() != null) {
            if (getLevel().isClientSide() && hasFluid()) {
                spawnParticles(ParticleTypes.ENTITY_EFFECT, 1, getFluidParticleColor());
            } else if (brewTimeRemaining == 0) {
                craftFromCurrentIngredients();
            }
        }

        if (hasBrew()) {
            brew.tick();
        }
    }

    public void spawnParticles(IParticleData particleData, int amount, double r, double g, double b) {
        if (getLevel() == null) {
            return;
        }

        double y = getBlockPos().getY() + getFluidLevel() + 4 / 16D + 0.5 / 16D;

        for (int i = 0; i < amount; i++) {
            double x = getBlockPos().getX() + getLevel().getRandom().nextDouble() * 26 / 16D + 3 / 16D;
            double z = getBlockPos().getZ() + getLevel().getRandom().nextDouble() * 26 / 16D + 3 / 16D;

            getLevel().addParticle(particleData, x, y, z, r, g, b);
        }
    }

    @Override
    public void spawnSplashParticles(double x, double z, double r, double g, double b) {
        if (getLevel() == null) {
            return;
        }

        double y = getBlockPos().getY() + getFluidLevel() + 4 / 16D + 0.5 / 16D;
        int amount = 6;

        for (int i = 0; i < amount; i++) {
            getLevel().addParticle(ModParticleTypes.CAULDRON_SPLASH.get(), x, y, z, r, g, b);
        }
    }

    private int getFluidParticleColor() {
        int color = 0xFFFFFF;
        if (hasFluid()) {
            FluidStack fluidStack = getFluid();
            Fluid fluid = fluidStack.getFluid();
            if (fluid != Fluids.WATER) {
                color = fluid.getAttributes().getColor(fluidStack);
            }
        }
        if ((color & 0xFFFFFF) == 0xFFFFFF) {
            color = Fluids.WATER.getAttributes().getColor(getLevel(), getBlockPos());
        }
        return ColorHelper.mixColors(color, BREWING_COLOR, brewingColorAlpha.get(0));
    }

    protected void onEntityInside(Entity entity, double yOffset) {
        if (hasBrew()) {
            getBrew().onEntityInside(entity, yOffset);
        } else if (entity instanceof ItemEntity) {
            onItemInside((ItemEntity) entity, yOffset);
        }
    }

    private void onItemInside(ItemEntity itemEntity, double yOffset) {
        CompoundNBT itemData = itemEntity.getPersistentData();

        if (!itemEntity.level.isClientSide()
                && itemEntity.getDeltaMovement().y() <= 0
                && !itemData.contains("InitialDeltaMovement", Constants.NBT.TAG_COMPOUND)
        ) {
            CompoundNBT nbt = new CompoundNBT();
            Vector3d deltaMovement = itemEntity.getDeltaMovement();

            nbt.putDouble("X", deltaMovement.x());
            nbt.putDouble("Y", deltaMovement.y());
            nbt.putDouble("Z", deltaMovement.z());

            itemData.put("InitialDeltaMovement", nbt);
        }

        if (fluidTank.isFull()
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

        if (isValidIngredient(itemEntity.getItem()) && !inventory.isFull()) {
            brewTimeRemaining = BREW_TIME;
            if (inventory.isEmpty()) {
                brewingColorAlpha.target(1);
            }
        }

        if (getLevel().isClientSide()) {
            spawnSplashParticles(itemEntity.getX(), itemEntity.getZ(), getFluidParticleColor());
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
            getLevel().playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), ModSoundEvents.CAULDRON_RETURN_INERT_INGREDIENT.get(), SoundCategory.BLOCKS, 0.5F, 1);
        }

        spawnInCauldron(remainder, motion);

        getLevel().playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), SoundEvents.GENERIC_SPLASH, SoundCategory.BLOCKS, 1, 1);
        itemEntity.remove();
    }

    private boolean isValidIngredient(ItemStack stack) {
        return !ModTags.INERT.contains(stack.getItem());
    }

    private void spawnInCauldron(ItemStack stack, Vector3d motion) {
        if (getLevel() == null) {
            return;
        }

        Vector3d position = getCenter();

        ItemEntity itemEntity = new ItemEntity(getLevel(), position.x(), position.y(), position.z(), stack);
        itemEntity.setDefaultPickUpDelay();
        itemEntity.setDeltaMovement(motion);
        getLevel().addFreshEntity(itemEntity);
    }

    private void craftFromCurrentIngredients() {
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

        CauldronRecipe<ResourceLocation> brewRecipe = findMatchingRecipe(manager, ModRecipeTypes.CAULDRON_BREWING);
        if (brewRecipe != null) {
            craftBrew(brewRecipe);
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
        return recipe.matches(getFluid(), inventory, this);
    }

    private <RESULT> RESULT assembleRecipe(CauldronRecipe<RESULT> recipe) {
        return recipe.assemble(getFluid(), inventory, this);
    }

    private void craftItem(CauldronRecipe<ItemStack> recipe) {
        ItemStack result = assembleRecipe(recipe);
        spawnInCauldron(result, new Vector3d(0, 0.5, 0));

        inventory.clear();
        fluidTank.clear();
        brewTimeRemaining = 0;
        sendUpdatePacket();
        setChanged();
    }

    private void craftFluid(CauldronRecipe<FluidStack> recipe) {
        FluidStack result = assembleRecipe(recipe);

        inventory.clear();
        if (!fluidTank.getFluid().isFluidEqual(result)) {
            setFluidOrBrewChanged();
        }
        fluidTank.setFluid(result);
        brewTimeRemaining = 0;
        sendUpdatePacket();
        setChanged();
    }

    private void craftBrew(CauldronRecipe<ResourceLocation> recipe) {
        ResourceLocation result = assembleRecipe(recipe);
        createBrew(result);
    }

    protected void createBrew(ResourceLocation brewTypeId) {
        BrewType<?> brewType = RecipeHelper.byType(ModRecipeTypes.BREW_TYPE).get(brewTypeId);

        if (brewType == null) {
            Caldera.LOGGER.error("Failed to load brew type {} for cauldron at {}", brewTypeId.toString(), getBlockPos());
            brew = null;
            return;
        }

        brew = brewType.assemble(getFluid(), inventory, this);

        inventory.clear();
        fluidTank.clear();
        brewTimeRemaining = 0;
        setFluidOrBrewChanged();
        sendUpdatePacket();
        setChanged();

        brew.onBrewed();
    }

    protected void setBrewToSludge() {
        createBrew(SLUDGE_TYPE);
    }

    protected void setFluidOrBrewChanged() {
        fluidOrBrewChanged = true;
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

        if (tag.getBoolean("containsItems")) {
            brewingColorAlpha.start(1);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = createUpdateTag(super.getUpdateTag());
        updateTag.putBoolean("containsItems", !inventory.isEmpty());
        return createUpdateTag(updateTag);
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
        float previousFluidLevel = (float) getFluidLevel();
        boolean wasEmpty = isEmpty();

        boolean fluidOrBrewChanged = nbt.getBoolean("FluidOrBrewChanged");
        if (fluidOrBrewChanged) {
            previousFluid = getFluid().copy();
            previousBrew = getBrew();
        }

        fluidTank.readFromNBT(nbt.getCompound("FluidHandler"));
        loadBrew(nbt);

        if (fluidOrBrewChanged) {
            brewingColorAlpha.start(0);
            if (isEmpty()) {
                previousBrew = null;
                previousFluid = FluidStack.EMPTY;
                fluidLevel.start(0);
                previousFluidAlpha.start(0);
                fluidAlpha.start(1);
            } else {
                fluidLevel.start(previousFluidLevel).target((float) getFluidLevel());
                if (!wasEmpty) {
                    previousFluidAlpha.start(1).target(0);
                    fluidAlpha.start(0).target(1);
                }
                if (brew != null) {
                    brew.onBrewed();
                }
            }
        } else {
            fluidLevel.target((float) getFluidLevel());
        }
    }

    protected CompoundNBT createUpdateTag(CompoundNBT nbt) {
        nbt.putBoolean("FluidOrBrewChanged", fluidOrBrewChanged);
        fluidOrBrewChanged = false;

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
