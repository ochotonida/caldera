package caldera.block.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class MultiblockEntity<BE extends MultiblockEntity<BE>> extends BlockEntity {

    private final Multiblock<?, ?> multiblock;
    private final boolean isController;

    public MultiblockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        if (!(state.getBlock() instanceof Multiblock<?, ?>)) {
            throw new IllegalArgumentException("Not a multiblock: " + state.getBlock());
        }
        this.multiblock = ((Multiblock<?, ?>) state.getBlock());
        this.isController = multiblock.isOrigin(state);
    }

    public boolean isController() {
        return isController;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public BE getController() {
        if (isController()) {
            return (BE) this;
        }
        if (level != null) {
            BlockPos origin = multiblock.getOrigin(getBlockState(), getBlockPos());
            BlockState originState = level.getBlockState(origin);

            if (multiblock.isOrigin(originState)) {
                BlockEntity blockEntity = level.getBlockEntity(origin);
                if (blockEntity != null && blockEntity.getType() == this.getType()) {
                    return (BE) blockEntity;
                }
            }
        }
        return null;
    }
}
