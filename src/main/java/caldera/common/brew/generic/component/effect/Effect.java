package caldera.common.brew.generic.component.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;

public interface Effect {

    /**
     * Called every tick for every active effect, in the order they were added to the brew
     */
    default void tick() {

    }

    /**
     * Called for each effect when an item is thrown into the cauldron, in the order the effects were added to the brew.
     * The item entity should not be modified, except for the item count. If the item stack was not fully consumed
     * by an effect, the remainder is passed to the next effect.
     * The item has already been removed from the world when this method is called.
     */
    default void consumeItem(ItemEntity itemEntity) {

    }

    void save(CompoundTag tag);
}
