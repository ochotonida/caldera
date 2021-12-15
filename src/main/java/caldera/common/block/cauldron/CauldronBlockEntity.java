package caldera.common.block.cauldron;

import caldera.Caldera;
import caldera.common.brew.Brew;
import caldera.common.brew.BrewType;
import caldera.common.brew.BrewTypeManager;
import caldera.common.init.*;
import caldera.common.network.BrewUpdatePacket;
import caldera.common.network.NetworkHandler;
import caldera.common.recipe.cauldron.CauldronRecipe;
import caldera.common.util.CraftingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;

public class CauldronBlockEntity extends BlockEntity implements Cauldron {

    public static final BlockEntityTicker<CauldronBlockEntity> TICKER = (level, pos, state, blockEntity) -> blockEntity.tick();

    private static final ResourceLocation SLUDGE_TYPE = new ResourceLocation(Caldera.MODID, "sludge");
    private static final int BREW_TIME = 60;

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> EmptyHandler.INSTANCE);
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> EmptyFluidHandler.INSTANCE);
    protected final CauldronFluidTank fluidTank;
    protected final CauldronItemHandler inventory;
    private Brew brew;
    private int brewingTimeRemaining;
    // private final CauldronUpdatePacket queuedMessages

    protected static final int BREWING_COLOR = 0xA3C740;

    private final TransitionHelper transitionHelper;

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get(), pos, state);
        fluidTank = new CauldronFluidTank(this);
        inventory = new CauldronItemHandler(this);
        transitionHelper = new TransitionHelper(this);
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

    public TransitionHelper getTransitionHelper() {
        return transitionHelper;
    }

    public boolean hasBrew() {
        return getBrew() != null;
    }

    public boolean hasFluid() {
        return !getFluid().isEmpty();
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

    public float getVisualFluidLevel(float partialTicks) {
        if (hasBrew()) {
            return getBrew().getVisualFluidLevel(partialTicks);
        }
        return (float) getFluidLevel();
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

    public void onRemove() {
        sendBrewUpdate();
    }

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
                spawnParticles(ModParticleTypes.CAULDRON_BUBBLE.get(), 2, transitionHelper.getParticleColor());
            }
        }

        transitionHelper.tick();
    }

    private void updateBrewing() {
        if (brewingTimeRemaining >= 0 && getLevel() != null) {
            if (getLevel().isClientSide() && hasFluid()) {
                spawnParticles(ParticleTypes.ENTITY_EFFECT, 1, transitionHelper.getParticleColor());
            } else if (brewingTimeRemaining == 0) {
                craftFromCurrentIngredients();
            }
            brewingTimeRemaining--;
        }

        if (hasBrew()) {
            brew.tick();
        }

        sendBrewUpdate();
    }

    public void spawnParticle(ParticleOptions particleData, double xOffset, double yOffset, double zOffset, double xSpeed, double ySpeed, double zSpeed, boolean useFluidHeight) {
        if (level == null) {
            return;
        }

        Vec3 center = getCenter();

        double x = center.x() + xOffset;
        double y = center.y() + yOffset;
        double z = center.z() + zOffset;

        if (useFluidHeight) {
            y += getFluidLevel();
        }

        level.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    protected void onEntityInside(Entity entity, double yOffset) {
        if (entity instanceof ItemEntity) {
            onItemInside((ItemEntity) entity, yOffset);
        }

        if (hasBrew()) {
            getBrew().onEntityInside(entity, yOffset);
        }
    }

    private void onItemInside(ItemEntity itemEntity, double yOffset) {
        if (getLevel() == null || getLevel().isClientSide()) {
            return;
        }

        CompoundTag itemData = itemEntity.getPersistentData();

        if (itemEntity.getDeltaMovement().y() <= 0 && !itemData.contains("InitialDeltaMovement", Tag.TAG_COMPOUND)) {
            CompoundTag nbt = new CompoundTag();
            Vec3 deltaMovement = itemEntity.getDeltaMovement();

            nbt.putDouble("X", deltaMovement.x());
            nbt.putDouble("Y", deltaMovement.y());
            nbt.putDouble("Z", deltaMovement.z());

            itemData.put("InitialDeltaMovement", nbt);
        }

        if (!hasBrew() && fluidTank.isFull() && itemEntity.getDeltaMovement().y() <= 0 && yOffset < 0.2) {
            addItemToCauldron(itemEntity);
        }
    }

    private void addItemToCauldron(ItemEntity itemEntity) {
        if (isValidIngredient(itemEntity.getItem()) && !inventory.isFull()) {
            brewingTimeRemaining = BREW_TIME;
            if (inventory.isEmpty()) {
                transitionHelper.startColorTransition();
            }
            sendBlockUpdated();
        }

        Vec3 motion = Cauldron.getInitialDeltaMovement(itemEntity);

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
                discardItem(containerStack, motion);
            }

            inventory.addItem(stack);
        } else {
            itemEntity.level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), ModSoundEvents.CAULDRON_RETURN_INERT_INGREDIENT.get(), SoundSource.BLOCKS, 0.5F, 1);
        }

        discardItem(remainder, motion);

        itemEntity.level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1, 1);
        itemEntity.discard();
    }

    private boolean isValidIngredient(ItemStack stack) {
        return !ModTags.INERT.contains(stack.getItem());
    }

    public void discardItem(ItemStack stack, Vec3 previousMotion) {
        if (getLevel() == null) {
            return;
        }

        Vec3 motion = previousMotion
                .multiply(-1, 0, -1)
                .normalize()
                .scale(0.2)
                .add(0, 0.425, 0);

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

        CauldronRecipe<ItemStack> itemRecipe = findMatchingRecipe(ModRecipeTypes.CAULDRON_ITEM_CRAFTING);
        if (itemRecipe != null) {
            craftItem(itemRecipe);
            return;
        }

        CauldronRecipe<FluidStack> fluidRecipe = findMatchingRecipe(ModRecipeTypes.CAULDRON_FLUID_CRAFTING);
        if (fluidRecipe != null) {
            craftFluid(fluidRecipe);
            return;
        }

        CauldronRecipe<ResourceLocation> brewRecipe = findMatchingRecipe(ModRecipeTypes.CAULDRON_BREWING);
        if (brewRecipe != null) {
            craftBrew(brewRecipe);
            return;
        }

        if (inventory.isFull()) {
            setBrewToSludge();
        }
    }

    @Nullable
    private <RECIPE extends CauldronRecipe<?>> RECIPE findMatchingRecipe(RecipeType<RECIPE> type) {
        // noinspection ConstantConditions
        Collection<RECIPE> itemRecipes = CraftingHelper.getRecipesByType(getLevel().getRecipeManager(), type);
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
        spawnItem(result);

        inventory.clear();
        fluidTank.clear();
        brewingTimeRemaining = 0;
        sendBlockUpdated();
        setChanged();
    }

    private void craftFluid(CauldronRecipe<FluidStack> recipe) {
        FluidStack result = assembleRecipe(recipe);

        inventory.clear();
        transitionHelper.setPreviousFluidAndBrew(getFluid(), getBrew());
        transitionHelper.startFluidTransition();
        fluidTank.setFluid(result);
        brewingTimeRemaining = 0;
        sendBlockUpdated();
        setChanged();
    }

    private void craftBrew(CauldronRecipe<ResourceLocation> recipe) {
        ResourceLocation result = assembleRecipe(recipe);
        createBrew(result);
    }

    protected void createBrew(ResourceLocation brewTypeId) {
        BrewType brewType = BrewTypeManager.get(brewTypeId);

        if (brewType == null) {
            Caldera.LOGGER.error("Failed to load brew type {} for cauldron at {}", brewTypeId.toString(), getBlockPos());
            return;
        }

        transitionHelper.setPreviousFluidAndBrew(getFluid(), getBrew());

        brew = brewType.assemble(getFluid(), inventory, this);

        inventory.clear();
        fluidTank.clear();
        brewingTimeRemaining = 0;
        transitionHelper.startFluidTransition();
        sendUpdatePacket();
        setChanged();

        brew.onBrewed();
        sendBrewUpdate();
    }

    protected void setBrewToSludge() {
        createBrew(SLUDGE_TYPE);
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

    protected void sendBrewUpdate() {
        if (getLevel() == null || getLevel().isClientSide()) {
            return;
        }
        if (brew == null || !brew.hasUpdate()) {
            return;
        }

        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> getLevel().getChunkAt(getBlockPos())),
                new BrewUpdatePacket(getBlockPos(), brew.getUpdateTag())
        );

        brew.clearUpdate();
    }

    protected void sendBlockUpdated() {
        if (getLevel() instanceof ServerLevel level) {
            level.getChunkSource().blockChanged(getBlockPos());
        }
    }

    // manually send an update packet immediately
    public void sendUpdatePacket() {
        if (getLevel() != null && !getLevel().isClientSide()) {
            ServerChunkCache chunkCache = ((ServerChunkCache) getLevel().getChunkSource());
            // noinspection ConstantConditions
            chunkCache.chunkMap
                    .getPlayers(new ChunkPos(getBlockPos()), false)
                    .forEach(player -> player.connection.send(getUpdatePacket()));
        }
    }

    // Called on the client when the block entity is being synced with the block entity on the server
    @Override
    public void onDataPacket(Connection networkManager, ClientboundBlockEntityDataPacket updatePacket) {
        if (updatePacket.getTag() != null) {
            readUpdateTag(updatePacket.getTag());
        }
    }

    // Called on the server when data is re-synced to the client on block update
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (!isController()) {
            return null;
        }
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void readUpdateTag(CompoundTag tag) {
        brewingTimeRemaining = tag.getInt("BrewingTimeRemaining");
        fluidTank.readFromNBT(tag.getCompound("FluidHandler"));
        inventory.deserializeNBT(tag.getCompound("ItemHandler"));
        brew = Brew.fromNbt(tag.getCompound("Brew"), this);
        transitionHelper.load(tag.getCompound("Transition"));
    }

    protected CompoundTag createUpdateTag(CompoundTag tag) {
        tag.putInt("BrewingTimeRemaining", brewingTimeRemaining);
        tag.put("FluidHandler", fluidTank.writeToNBT(new CompoundTag()));
        tag.put("ItemHandler", inventory.serializeNBT());
        if (hasBrew()) {
            tag.put("Brew", brew.toNbt());
        }
        tag.put("Transition", transitionHelper.save());

        return tag;
    }

    // called on the client when loading a chunk from the server,
    // either during initial loading or when several blocks have changed at once
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readUpdateTag(tag);
    }

    // called on the server when a client loads a new chunk or when several blocks change at once
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        return createUpdateTag(updateTag);
    }

    // called on the server when loading the chunk
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        brewingTimeRemaining = tag.getInt("BrewingTimeRemaining");
        fluidTank.readFromNBT(tag.getCompound("FluidHandler"));
        inventory.deserializeNBT(tag.getCompound("ItemHandler"));
        brew = Brew.fromNbt(tag.getCompound("Brew"), this);

        if (!inventory.isEmpty() && fluidTank.isFull()) {
            transitionHelper.setColored();
        }
    }

    // called on the server when saving the chunk
    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("BrewingTimeRemaining", brewingTimeRemaining);
        tag.put("FluidHandler", fluidTank.writeToNBT(new CompoundTag()));
        tag.put("ItemHandler", inventory.serializeNBT());
        if (hasBrew()) {
            tag.put("Brew", getBrew().toNbt());
        }
    }
}
