package caldera.common.brew.generic;

import caldera.common.util.ChasingValue;
import caldera.common.util.ColorHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ColorInfo {

    private int baseColor;
    private int overlayColor;
    private int previousBaseColor;
    private int previousOverlayColor;

    private final ChasingValue progress;

    public ColorInfo() {
        this.progress = new ChasingValue(1/30F, 1);
        this.baseColor = this.previousBaseColor = 0xFFFFFF;
        this.overlayColor = this.previousOverlayColor = 0xFFFFFF;
    }

    public void tick() {
        progress.tick();
    }

    public int getBaseColor(float partialTicks) {
        return blend(previousBaseColor, baseColor, partialTicks);
    }

    public int getOverlayColor(float partialTicks) {
        return blend(previousOverlayColor, overlayColor, partialTicks);
    }

    private int blend(int previousColor, int newColor, float partialTicks) {
        float amount = progress.getValue(partialTicks);
        return ColorHelper.mixColors(previousColor, newColor, amount);
    }

    public boolean hasSettled() {
        return progress.getValue() == progress.getTarget();
    }

    public void changeColor(int baseColor, int overlayColor, int transitionTime) {
        previousBaseColor = getBaseColor(0);
        previousOverlayColor = getOverlayColor(0);

        this.baseColor = baseColor;
        this.overlayColor = overlayColor;
        progress.setValue(0);

        if (transitionTime > 0) {
            progress.setStep(1F / transitionTime);
        } else {
            previousBaseColor = baseColor;
            previousOverlayColor = overlayColor;
        }
    }

    public CompoundTag save() {
        CompoundTag result = new CompoundTag();
        result.putInt("BaseColor", baseColor);
        result.putInt("OverlayColor", overlayColor);
        if (previousBaseColor != baseColor || previousOverlayColor != overlayColor) {
            result.putInt("PreviousBaseColor", previousBaseColor);
            result.putInt("PreviousOverlayColor", previousOverlayColor);
            if (progress.getValue() != progress.getTarget()) {
                result.put("Progress", progress.save());
            }
        }

        return result;
    }

    public void load(CompoundTag tag) {
        previousBaseColor = baseColor = tag.getInt("BaseColor");
        previousOverlayColor = overlayColor = tag.getInt("OverlayColor");
        if (tag.contains("PreviousBaseColor", Tag.TAG_INT)) {
            previousBaseColor = tag.getInt("PreviousBaseColor");
        }
        if (tag.contains("PreviousOverlayColor", Tag.TAG_INT)) {
            previousOverlayColor = tag.getInt("PreviousOverlayColor");
        }
        if (tag.contains("Progress", Tag.TAG_COMPOUND)) {
            progress.load(tag.getCompound("Progress"));
        }
    }
}
