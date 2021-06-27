package caldera.common.recipe.sludge;

import caldera.common.recipe.Brew;
import net.minecraft.nbt.CompoundNBT;

public class SludgeBrew implements Brew {

    private final SludgeBrewType type;
    private final int color;

    public SludgeBrew(SludgeBrewType type, int color) {
        this.type = type;
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
    public void writeBrew(CompoundNBT nbt) {

    }
}
