package caldera.common.recipe.brew.sludge;

import caldera.common.recipe.Cauldron;
import caldera.common.recipe.brew.Brew;
import caldera.common.util.ColorHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;

public class SludgeBrew implements Brew {

    private final SludgeBrewType type;
    private final Cauldron cauldron;
    private final int color;
    private final int particleColor;

    private int ticks;

    public SludgeBrew(SludgeBrewType type, Cauldron cauldron, int color) {
        this.type = type;
        this.cauldron = cauldron;
        this.color = color;
        this.particleColor = ColorHelper.fromRGB(
                ColorHelper.getRed(color) / 2,
                ColorHelper.getGreen(color) / 2,
                ColorHelper.getBlue(color) / 2
        );
    }

    @Override
    public SludgeBrewType getType() {
        return type;
    }

    @Override
    public int getColor(float partialTicks) {
        return color;
    }

    @Override
    public void onBrewed() {
        cauldron.spawnParticles(ParticleTypes.ENTITY_EFFECT, 50, particleColor);
    }

    @Override
    public void tick() {
        ticks++;

        if (ticks % 2 == 0 && cauldron.getLevel() != null && cauldron.getLevel().isClientSide()) {
            cauldron.spawnParticles(ParticleTypes.ENTITY_EFFECT, 1, particleColor);
        }
    }

    @Override
    public void writeBrew(CompoundNBT nbt) {

    }
}
