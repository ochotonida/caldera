package caldera.common.recipe;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public interface Brew {

    void entityInside(Entity entity);

    void writeBrew(CompoundNBT nbt);

    default void writeBrew(PacketBuffer buffer) {
        CompoundNBT nbt = new CompoundNBT();
        writeBrew(nbt);
        buffer.writeNbt(nbt);
    }
}
