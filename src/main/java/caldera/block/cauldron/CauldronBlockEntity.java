package caldera.block.cauldron;

import caldera.Caldera;
import caldera.block.cauldron.contents.CauldronContents;
import caldera.block.cauldron.contents.FluidContents;
import caldera.block.multiblock.MultiblockEntity;
import caldera.registry.ModBlockEntityTypes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class CauldronBlockEntity extends MultiblockEntity<CauldronBlockEntity> implements Cauldron {

    public static final BlockEntityTicker<CauldronBlockEntity> TICKER = (level, pos, state, blockEntity) -> blockEntity.tick();

    @Nullable
    private CauldronContents contents;
    @Nullable
    private final IItemHandler itemHandler;
    @Nullable
    private final IFluidHandler fluidHandler;

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get(), pos, state);
        contents = isController() ? new FluidContents() : null;
        itemHandler = isController() ? new CauldronItemHandler(this) : null;
        fluidHandler = isController() ? new CauldronFluidHandler(this) : null;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public void tick() {

    }

    @Override
    public CauldronContents getContents() {
        CauldronBlockEntity controller = getController();
        if (controller != null) {
            return controller.contents;
        }
        return null;
    }

    @Override
    public void setContents(CauldronContents contents) {
        if (!isController()) {
            throw new UnsupportedOperationException();
        }
        this.contents = contents;
        if (getLevel() != null && !getLevel().isClientSide) {
            setChanged();
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Vec3 getCenter() {
        CauldronBlockEntity controller = getController();

        if (controller == null) {
            return Vec3.ZERO;
        }

        double floorHeight = 4 / 16D;
        BlockPos pos = controller.getBlockPos();
        return new Vec3(pos.getX() + 1, pos.getY() + floorHeight, pos.getZ() + 1);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if (isController()) {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        return null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (isController()) {
            tag.put("contents", CauldronContents.CODEC.encodeStart(NbtOps.INSTANCE, contents).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (isController()) {
            if (tag.contains("contents")) {
                CauldronContents.CODEC
                        .decode(NbtOps.INSTANCE, tag.get("contents"))
                        .resultOrPartial(Util.prefix("Cauldron contents: ", Caldera.LOGGER::error))
                        .map(Pair::getFirst)
                        .ifPresent(this::setContents);
            }
        }
    }
}
