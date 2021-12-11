package caldera.common.brew.generic.component.effect;

import net.minecraft.nbt.CompoundTag;

public interface Effect {

    default void tick() {

    }

    void save(CompoundTag tag);
}
