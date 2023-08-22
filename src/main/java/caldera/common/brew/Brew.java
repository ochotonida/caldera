package caldera.common.brew;

import caldera.Caldera;
import caldera.common.block.cauldron.Cauldron;
import caldera.common.block.cauldron.CauldronBlockEntity;
import caldera.common.network.BrewUpdatePacket;
import caldera.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

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
     * Called when a player is about to destroy the cauldron this brew is contained in
     */
    public void onPlayerAboutToDestroy(Player player) {

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

    protected void sendUpdate(CompoundTag tag) {
        if (getCauldron().getLevel() == null || getCauldron().getLevel().isClientSide()) {
            return;
        }

        NetworkHandler.INSTANCE.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> getCauldron().getLevel().getChunkAt(getCauldron().getBlockPos())),
                new BrewUpdatePacket(getCauldron().getBlockPos(), tag)
        );
    }

    public void onUpdate(CompoundTag tag) {

    }

    /**
     * Write a brew of this type to nbt
     *
     * @param tag The compound tag to write the brew to
     */
    protected abstract void save(CompoundTag tag);

    /**
     * Load a brew of this type from nbt
     *
     * @param tag Compound tag to read the brew from
     */
    protected abstract void load(CompoundTag tag);

    public CompoundTag toNbt() {
        CompoundTag result = new CompoundTag();
        CompoundTag brewTag = new CompoundTag();
        save(brewTag);
        result.put("Brew", brewTag);
        result.putString("BrewType", getType().getId().toString());
        return result;
    }

    public static Brew fromNbt(CompoundTag tag, CauldronBlockEntity cauldron) {
        if (!tag.contains("BrewType", Tag.TAG_STRING)) {
            return null;
        }

        String brewTypeIdString = tag.getString("BrewType");
        ResourceLocation brewTypeId = ResourceLocation.tryParse(brewTypeIdString);

        if (brewTypeId == null) {
            Caldera.LOGGER.error("The cauldron at {} tried to load invalid brew type {}. The brew will be discarded.", cauldron.getBlockPos(), brewTypeIdString);
            return null;
        }

        BrewType brewType = BrewTypeManager.get(brewTypeId);
        if (brewType == null) {
            Caldera.LOGGER.error("The cauldron at {} tried to load unknown brew type {}. The brew will be discarded.", cauldron.getBlockPos(), brewTypeIdString);
            return null;
        }

        Brew result = brewType.create(cauldron);
        result.load(tag.getCompound("Brew"));

        return result;
    }
}
