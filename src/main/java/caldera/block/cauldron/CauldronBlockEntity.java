package caldera.block.cauldron;

import caldera.block.cauldron.contents.CauldronContents;
import caldera.block.cauldron.contents.FluidContents;
import caldera.block.multiblock.MultiblockEntity;
import caldera.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class CauldronBlockEntity extends MultiblockEntity<CauldronBlockEntity> implements Cauldron {

    public static final BlockEntityTicker<CauldronBlockEntity> TICKER = (level, pos, state, blockEntity) -> blockEntity.tick();

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
            return contents;
        }
        return null;
    }

    @Override
    public void setContents(CauldronContents contents) {
        if (!isController()) {
            throw new UnsupportedOperationException();
        }
        this.contents = contents;
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
}
