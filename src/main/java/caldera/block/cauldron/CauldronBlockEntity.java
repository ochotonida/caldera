package caldera.block.cauldron;

import caldera.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class CauldronBlockEntity extends BlockEntity {

    public static final BlockEntityTicker<CauldronBlockEntity> TICKER = (level, pos, state, blockEntity) -> blockEntity.tick();

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get(), pos, state);
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

    public void tick() {

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
}
