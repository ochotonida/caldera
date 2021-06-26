package caldera.common.recipe;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public interface Brew {


    /**
     * @return The height of the fluid in the cauldron (in blocks)
     */
    double getFluidLevel();

    /**
     * Returns the color of the brew including alpha
     * @return alpha << 24 + red << 16 + green << 8 + blue << 0
     */
    int getColor();

    /**
     * Called every tick for every entity inside the cauldron
     *
     * @param entity an entity inside the cauldron
     * @param yOffset the vertical offset of the entity from the bottom of the cauldron
     */
    void onEntityInside(Entity entity, double yOffset);

    void writeBrew(CompoundNBT nbt);

    default void writeBrew(PacketBuffer buffer) {
        CompoundNBT nbt = new CompoundNBT();
        writeBrew(nbt);
        buffer.writeNbt(nbt);
    }
}
