package caldera.common.brew.sludge;

import caldera.common.brew.Brew;
import caldera.common.recipe.Cauldron;
import caldera.common.util.ColorHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;

public class SludgeBrew extends Brew {

    private final int color;
    private final int particleColor;

    private int ticks;

    public SludgeBrew(SludgeBrewType type, Cauldron cauldron, int color) {
        super(type, cauldron);
        this.color = color;
        this.particleColor = ColorHelper.fromRGB(
                ColorHelper.getRed(color) / 2,
                ColorHelper.getGreen(color) / 2,
                ColorHelper.getBlue(color) / 2
        );
    }

    @Override
    public int getColor(float partialTicks) {
        return color;
    }

    @Override
    public void onBrewed() {
        getCauldron().spawnParticles(ParticleTypes.ENTITY_EFFECT, 50, particleColor);
    }

    @Override
    public void tick() {
        ticks++;

        if (ticks % 2 == 0 && getCauldron().getLevel() != null && getCauldron().getLevel().isClientSide()) {
            getCauldron().spawnParticles(ParticleTypes.ENTITY_EFFECT, 1, particleColor);
        }
    }

    @Override
    public void save(CompoundTag nbt) {

    }

    @Override
    public void load(CompoundTag tag) {

    }
}
