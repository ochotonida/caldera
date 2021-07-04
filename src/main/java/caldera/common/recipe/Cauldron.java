package caldera.common.recipe;

import caldera.client.util.ColorHelper;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface Cauldron {

    @Nullable
    World getLevel();

    boolean hasLevel();

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
    Vector3d getCenter();

    /**
     * Spawn particles at random positions in the cauldron at the current fluid height
     * (for particles that use the speed parameters for coloring)
     *
     * @param particleData the particle to spawn
     * @param amount the amount of particles to spawn
     * @param color the color of the particles
     */
    default void spawnParticles(IParticleData particleData, int amount, int color) {
        double r = ColorHelper.getRed(color) / 255D;
        double g = ColorHelper.getGreen(color) / 255D;
        double b = ColorHelper.getBlue(color) / 255D;

        spawnParticles(particleData, amount, r, g, b);
    }

    /**
     * Spawn particles at random positions in the cauldron at the current fluid height
     *
     * @param particleData the particle to spawn
     * @param amount the amount of particles to spawn
     */
    void spawnParticles(IParticleData particleData, int amount, double xSpeed, double ySpeed, double zSpeed);
}
