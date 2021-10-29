package caldera.common.block.cauldron;

import caldera.common.brew.Brew;
import caldera.common.util.ChasingValue;
import caldera.common.util.ColorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class TransitionHelper {

    private final CauldronBlockEntity cauldron;

    private final ChasingValue fluidAlphaProgress;
    private final ChasingValue fluidColorProgress;

    private FluidStack previousFluid;
    private Brew previousBrew;

    public TransitionHelper(CauldronBlockEntity cauldron) {
        this.cauldron = cauldron;

        fluidAlphaProgress = new ChasingValue(1/30F, 1);
        fluidColorProgress = new ChasingValue(1/30F, 0);

        previousFluid = FluidStack.EMPTY;
        previousBrew = null;
    }

    public FluidStack getPreviousFluid() {
        return previousFluid;
    }

    public Brew getPreviousBrew() {
        return previousBrew;
    }

    public float getFluidAlpha(float partialTicks) {
        return Math.min(1, fluidAlphaProgress.getValue(partialTicks) * 3 / 2F);
    }

    public float getPreviousFluidAlpha(float partialTicks) {
        return Math.max(0, 1 - (fluidAlphaProgress.getValue(partialTicks) - 1 / 3F) * (3 / 2F));
    }

    public float getFluidColor(float partialTicks) {
        return fluidColorProgress.getValue(partialTicks);
    }

    public void setPreviousFluidAndBrew(FluidStack fluid, Brew brew) {
        previousFluid = fluid.copy();
        previousBrew = brew;
    }

    public void resetColor() {
        fluidColorProgress.reset(0);
    }

    public void setColored() {
        fluidColorProgress.reset(1);
    }

    public void startColorTransition() {
        fluidColorProgress.setTarget(1);
    }

    public void startFluidTransition() {
        resetColor();
        fluidAlphaProgress.setValue(0);
    }

    public void tick() {
        fluidAlphaProgress.tick();
        fluidColorProgress.tick();

        if (fluidAlphaProgress.getValue() == fluidAlphaProgress.getTarget()) {
            previousBrew = null;
            previousFluid = FluidStack.EMPTY;
        }
    }

    public int getParticleColor() {
        int color = 0xFFFFFF;
        if (cauldron.hasFluid()) {
            FluidStack fluidStack = cauldron.getFluid();
            Fluid fluid = fluidStack.getFluid();
            if (fluid != Fluids.WATER) {
                color = fluid.getAttributes().getColor(fluidStack);
            }
        }
        if ((color & 0xFFFFFF) == 0xFFFFFF) {
            color = Fluids.WATER.getAttributes().getColor(cauldron.getLevel(), cauldron.getBlockPos());
        }
        return ColorHelper.mixColors(color, CauldronBlockEntity.BREWING_COLOR, getFluidColor(0));
    }

    public CompoundTag save() {
        CompoundTag result = new CompoundTag();
        if (previousBrew != null) {
            result.put("PreviousBrew", CauldronBlockEntity.saveBrew(previousBrew));
        } else if (!previousFluid.isEmpty()) {
            CompoundTag fluidTag = new CompoundTag();
            previousFluid.writeToNBT(fluidTag);
            result.put("PreviousFluid", fluidTag);
        }

        result.put("FluidAlphaProgress", saveValue(fluidAlphaProgress));
        result.put("FluidColorProgress", saveValue(fluidColorProgress));

        return result;
    }

    public void load(CompoundTag tag) {
        previousBrew = CauldronBlockEntity.loadBrew(tag.getCompound("PreviousBrew"), cauldron);
        previousFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("PreviousFluid"));

        loadValue(tag.getCompound("FluidAlphaProgress"), fluidAlphaProgress);
        loadValue(tag.getCompound("FluidColorProgress"), fluidColorProgress);
    }

    private static CompoundTag saveValue(ChasingValue value) {
        CompoundTag result = new CompoundTag();
        result.putFloat("Value", value.getValue());
        result.putFloat("Target", value.getTarget());
        return result;
    }

    private static void loadValue(CompoundTag tag, ChasingValue value) {
        value.setValue(tag.getFloat("Value"));
        value.setTarget(tag.getFloat("Target"));
    }
}
