package caldera.common.block.cauldron;

import caldera.common.brew.Brew;
import caldera.common.util.ColorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public interface Cauldron {

    @Nullable
    Level getLevel();

    boolean isRemoved();

    /**
     * Returns the position of this cauldron
     *
     * @return the position of the bottom north-western corner of the cauldron
     */
    BlockPos getBlockPos();

    /**
     * Returns a vector located at the center of this cauldron
     *
     * @return The center of this cauldron, at the bottom of the basin
     */
    Vec3 getCenter();

    Brew getBrew();

    void setChanged();

    /**
     * Spawns an item stack inside the cauldron
     *
     * @param stack the item to spawn
     * @param previousMotion the motion of an item previously thrown into the cauldron. The discarded item is thrown back
     *                       into the direction it came from
     */
    void discardItem(ItemStack stack, Vec3 previousMotion);

    /**
     * Spawns an item inside the cauldron
     */
    default void spawnItem(ItemStack stack) {
        discardItem(stack, Vec3.ZERO);
    }

    /**
     * Spawn particles at random positions in the cauldron at the current fluid height
     * (for particles that use the speed parameters for coloring)
     *
     * @param particleData the particle to spawn
     * @param amount the amount of particles to spawn
     * @param color the color of the particles
     */
    default void spawnParticles(ParticleOptions particleData, int amount, int color) {
        if (getLevel() == null) {
            return;
        }

        double r = ColorHelper.getRed(color) / 255D;
        double g = ColorHelper.getGreen(color) / 255D;
        double b = ColorHelper.getBlue(color) / 255D;

        double yOffset = 0.5 / 16D;

        for (int i = 0; i < amount; i++) {
            double xOffset = (getLevel().getRandom().nextDouble() * 2 - 1) * 13 / 16;
            double zOffset = (getLevel().getRandom().nextDouble() * 2 - 1) * 13 / 16;

            spawnParticle(particleData, xOffset, yOffset, zOffset, r, g, b, true);
        }
    }

    /**
     * Spawn particles at random positions in the cauldron at the current fluid height
     *
     * @param particle the particle to spawn
     */
    void spawnParticle(ParticleOptions particle, double xOffset, double yOffset, double zOffset, double xSpeed, double ySpeed, double zSpeed, boolean useFluidHeight);

    default List<Entity> getEntitiesInRange(double range, Predicate<Entity> predicate) {
        if (getLevel() == null) {
            return Collections.emptyList();
        }
        Vec3 center = getCenter();
        Predicate<Entity> combinedPredicate = entity -> predicate.test(entity) && entity.distanceToSqr(center) <= range * range;
        return getLevel().getEntities(
                (Entity) null,
                new AABB(
                        center.x - range, center.y - range, center.z - range,
                        center.x + range, center.y + range, center.z + range),
                combinedPredicate
        );
    }

    /**
     * Returns the initial motion vector this item entity had as it entered the cauldron,
     * or it's current motion if it never entered a cauldron
     */
    static Vec3 getInitialDeltaMovement(ItemEntity itemEntity) {
        CompoundTag itemData = itemEntity.getPersistentData();
        if (itemData.contains("InitialDeltaMovement", Tag.TAG_COMPOUND)) {
            CompoundTag nbt = itemData.getCompound("InitialDeltaMovement");
            return new Vec3(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
        }
        return itemEntity.getDeltaMovement();
    }
}
