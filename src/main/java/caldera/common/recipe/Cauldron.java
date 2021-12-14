package caldera.common.recipe;

import caldera.common.brew.Brew;
import caldera.common.util.ColorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public interface Cauldron {

    @Nullable
    Level getLevel();

    boolean hasLevel();

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
}
