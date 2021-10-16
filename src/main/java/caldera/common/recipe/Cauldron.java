package caldera.common.recipe;

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

    /**
     * Spawn particles at random positions in the cauldron at the current fluid height
     * (for particles that use the speed parameters for coloring)
     *
     * @param particleData the particle to spawn
     * @param amount the amount of particles to spawn
     * @param color the color of the particles
     */
    default void spawnParticles(ParticleOptions particleData, int amount, int color) {
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
    void spawnParticles(ParticleOptions particleData, int amount, double xSpeed, double ySpeed, double zSpeed);

    /**
     * Spawn colored splash particles int the cauldron
     * @param color the color of the particles
     */
    default void spawnSplashParticles(double x, double z, int color) {
        double r = ColorHelper.getRed(color) / 255D;
        double g = ColorHelper.getGreen(color) / 255D;
        double b = ColorHelper.getBlue(color) / 255D;

        spawnSplashParticles(x, z, r, g, b);
    }

    /**
     * Spawn colored splash particles in the cauldron
     */
    void spawnSplashParticles(double x, double z, double r, double g, double b);
}
