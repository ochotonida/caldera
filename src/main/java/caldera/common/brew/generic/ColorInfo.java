package caldera.common.brew.generic;

import caldera.common.util.ChasingValue;
import caldera.common.util.ColorHelper;

public class ColorInfo {

    private int color;
    private int previousColor;
    // TODO add alpha
    // TODO save transition as well

    private final ChasingValue progress;

    public ColorInfo() {
        this.progress = new ChasingValue(1/30F, 1);
        start(0xFFFFFF);
    }

    public void start(int color) {
        this.color = this.previousColor = color;
    }

    public void tick() {
        progress.tick();
    }

    public int getColor(float partialTicks) {
        float amount = progress.getValue(partialTicks);
        return ColorHelper.mixColors(color, previousColor, amount);
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
}
