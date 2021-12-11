package caldera.common.brew;

import caldera.common.recipe.Cauldron;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public abstract class Brew {

    private final BrewType brewType;
    private final Cauldron cauldron;

    protected Brew(BrewType brewType, Cauldron cauldron) {
        this.brewType = brewType;
        this.cauldron = cauldron;
    }

    public BrewType getType() {
        return brewType;
    }

    public Cauldron getCauldron() {
        return cauldron;
    }

    /**
     * @return The height of the fluid in the cauldron (in blocks)
     */
    public double getFluidLevel() {
        return 1;
    }

    public float getVisualFluidLevel(float partialTicks) {
        return (float) getFluidLevel();
    }

    /**
     * @return The current color of the brew. Only called on the client
     */
    public int getColor(float partialTicks) {
        return 0xFFFFFF;
    }

    /**
     * @return The current transparency of the brew. Only called on the client
     */
    public int getAlpha(float partialTicks) {
        return 0xFF;
    }

    public final int getColorAndAlpha(float partialTicks) {
        return (getAlpha(partialTicks) & 0xFF) << 24 | (getColor(partialTicks) & 0xFFFFFF);
    }

    /**
     * Called immediately after this brew has been added to the cauldron
     */
    public void onBrewed() {

    }

    /**
     * Called once every tick
     */
    public void tick() {

    }

    /**
     * Called every tick for every entity inside the cauldron
     *
     * @param entity an entity inside the cauldron
     * @param yOffset the vertical offset of the entity from the bottom of the cauldron
     */
    public void onEntityInside(Entity entity, double yOffset) {

    }

    /**
     * Write a brew of this type to nbt
     *
     * @param tag The compound tag to write the brew to
     */
    public abstract void save(CompoundTag tag);

    /**
     * Load a brew of this type from nbt
     *
     * @param tag Compound tag to read the brew from
     */
    public abstract void load(CompoundTag tag);
}
