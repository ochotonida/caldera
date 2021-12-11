package caldera.common.brew.generic;

import caldera.common.util.ChasingValue;
import caldera.common.util.ColorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ColorInfo {

    private int color;
    private int previousColor;
    // TODO add alpha

    private final ChasingValue progress;

    public ColorInfo() {
        this.progress = new ChasingValue(1/30F, 1);
        this.color = this.previousColor = 0xFFFFFF;
    }

    public void tick() {
        progress.tick();
    }

    public int getColor(float partialTicks) {
        float amount = progress.getValue(partialTicks);
        return ColorHelper.mixColors(previousColor, color, amount);
    }

    public boolean hasSettled() {
        return progress.getValue() == progress.getTarget();
    }

    public void changeColor(int newColor, int transitionTime) {
        previousColor = getColor(0);
        color = newColor;
        progress.setValue(0);

        if (transitionTime > 0) {
            progress.setStep(1F / transitionTime);
        } else {
            previousColor = newColor;
        }
    }

    public int getTargetColor() {
        return color;
    }

    public CompoundTag save() {
        CompoundTag result = new CompoundTag();
        result.putInt("Color", color);
        if (previousColor != color) {
            result.putInt("PreviousColor", previousColor);
            if (progress.getValue() != progress.getTarget()) {
                result.put("Progress", progress.save());
            }
        }

        return result;
    }

    public void load(CompoundTag tag) {
        previousColor = color = tag.getInt("Color");
        if (tag.contains("PreviousColor", Tag.TAG_INT)) {
            previousColor = tag.getInt("PreviousColor");
            if (tag.contains("Progress", Tag.TAG_COMPOUND)) {
                progress.load(tag.getCompound("Progress"));
            }
        }
    }
}
