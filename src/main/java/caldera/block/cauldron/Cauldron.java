package caldera.block.cauldron;

import caldera.block.cauldron.contents.CauldronContents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface Cauldron {

    CauldronContents getContents();

    void setContents(CauldronContents contents);

    Vec3 getCenter();

    Level getLevel();

    default void discardItem(ItemStack stack, Vec3 previousMotion) {
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
