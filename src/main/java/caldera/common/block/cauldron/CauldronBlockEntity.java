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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

public class CauldronBlockEntity extends BlockEntity implements Cauldron, TickableBlockEntity  {

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

    public Vec3 getCenter() {
        CauldronBlockEntity controller = getController();

        if (controller == null) {
            return Vec3.ZERO;
        }

        double floorHeight = 4 / 16D;
        BlockPos pos = controller.getBlockPos();
        return new Vec3(pos.getX() + 1, pos.getY() + floorHeight, pos.getZ() + 1);
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

    public void spawnParticles(ParticleOptions particleData, int amount, double r, double g, double b) {
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
        CompoundTag itemData = itemEntity.getPersistentData();

        if (!itemEntity.level.isClientSide()
                && itemEntity.getDeltaMovement().y() <= 0
                && !itemData.contains("InitialDeltaMovement", Constants.NBT.TAG_COMPOUND)
        ) {
            CompoundTag nbt = new CompoundTag();
            Vec3 deltaMovement = itemEntity.getDeltaMovement();

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

        CompoundTag itemData = itemEntity.getPersistentData();

        Vec3 motion;
        if (itemData.contains("InitialDeltaMovement", Constants.NBT.TAG_COMPOUND)) {
            CompoundTag nbt = itemData.getCompound("InitialDeltaMovement");
            motion = new Vec3(
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
            getLevel().playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), ModSoundEvents.CAULDRON_RETURN_INERT_INGREDIENT.get(), SoundSource.BLOCKS, 0.5F, 1);
        }

        spawnInCauldron(remainder, motion);

        getLevel().playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1, 1);
        itemEntity.remove();
    }

    private boolean isValidIngredient(ItemStack stack) {
        return !ModTags.INERT.contains(stack.getItem());
    }

    private void spawnInCauldron(ItemStack stack, Vec3 motion) {
        if (getLevel() == null) {
            return;
        }

        Vec3 position = getCenter();

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
    private <RECIPE extends CauldronRecipe<?>> RECIPE findMatchingRecipe(RecipeManager manager, RecipeType<RECIPE> type) {
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
        spawnInCauldron(result, new Vec3(0, 0.5, 0));

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

    protected InteractionResult onUse(Player player, InteractionHand hand) {
        if (player.level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (FluidUtil.interactWithFluidHandler(player, hand, fluidTank)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
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
    public AABB getRenderBoundingBox() {
        if (isController()) {
            return new AABB(getBlockPos(), getBlockPos().offset(2, 2, 2));
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
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        super.handleUpdateTag(state, tag);
        readUpdateTag(tag);

        if (tag.getBoolean("containsItems")) {
            brewingColorAlpha.start(1);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = createUpdateTag(super.getUpdateTag());
        updateTag.putBoolean("containsItems", !inventory.isEmpty());
        return createUpdateTag(updateTag);
    }

    // sync client on block update
    @Override
    public void onDataPacket(Connection networkManager, ClientboundBlockEntityDataPacket updatePacket) {
        readUpdateTag(updatePacket.getTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(getBlockPos(), 0, createUpdateTag(new CompoundTag()));
    }

    // load data sent to client
    protected void readUpdateTag(CompoundTag nbt) {
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

    protected CompoundTag createUpdateTag(CompoundTag nbt) {
        nbt.putBoolean("FluidOrBrewChanged", fluidOrBrewChanged);
        fluidOrBrewChanged = false;

        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundTag()));
        saveBrew(nbt);

        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundTag nbt) {
        super.load(state, nbt);
        fluidTank.readFromNBT(nbt.getCompound("FluidHandler"));
        inventory.deserializeNBT(nbt.getCompound("ItemHandler"));
        loadBrew(nbt);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("FluidHandler", fluidTank.writeToNBT(new CompoundTag()));
        nbt.put("ItemHandler", inventory.serializeNBT());
        saveBrew(nbt);

        return super.save(nbt);
    }

    private void loadBrew(CompoundTag nbt) {
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

    private void saveBrew(CompoundTag nbt) {
        if (getLevel() == null) {
            throw new IllegalArgumentException();
        }

        if (hasBrew()) {
            CompoundTag brewNBT = new CompoundTag();
            brew.writeBrew(brewNBT);
            nbt.put("Brew", brewNBT);
            nbt.putString("BrewType", brew.getType().getId().toString());
        }
    }
}
