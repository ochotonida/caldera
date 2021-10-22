package caldera.common.recipe.brew.generic.component.effect;

import net.minecraft.nbt.CompoundTag;

public interface Effect {

    default void tick() {

    }

    default void remove() {

    }

    void save(CompoundTag tag);
}
