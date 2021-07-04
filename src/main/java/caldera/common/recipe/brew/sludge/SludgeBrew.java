package caldera.common.recipe.brew.sludge;

import caldera.common.recipe.Cauldron;
import caldera.common.recipe.brew.Brew;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;

public class SludgeBrew implements Brew {

    private final SludgeBrewType type;
    private final Cauldron cauldron;
    private final int color;

    public SludgeBrew(SludgeBrewType type, Cauldron cauldron, int color) {
        this.type = type;
        this.cauldron = cauldron;
        this.color = color;
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
        cauldron.spawnParticles(ParticleTypes.ENTITY_EFFECT, 50, color);
    }

    @Override
    public void writeBrew(CompoundNBT nbt) {

    }
}
