package caldera.common.recipe.brew;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;

public interface Brew {

    /**
     * @return The brew type that can be used to load this brew
     */
    BrewType<?> getType();

    /**
     * @return The height of the fluid in the cauldron (in blocks)
     */
    default double getFluidLevel() {
        return 1;
    }

    int getColor(float partialTicks);

    default int getAlpha(float partialTicks) {
        return 0xFF;
    }

    default int getColorAndAlpha(float partialTicks) {
        return (getAlpha(partialTicks) & 0xFF) << 24 | (getColor(partialTicks) & 0xFFFFFF);
    }

    /**
     * Called every tick for every entity inside the cauldron
     *
     * @param entity an entity inside the cauldron
     * @param yOffset the vertical offset of the entity from the bottom of the cauldron
     */
    default void onEntityInside(Entity entity, double yOffset) {

    }

    void writeBrew(CompoundNBT nbt);
}