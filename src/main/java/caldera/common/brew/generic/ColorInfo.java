package caldera.common.brew.generic;

import caldera.common.util.ColorHelper;
import caldera.common.util.rendering.InterpolatedLinearChasingValue;

public class ColorInfo {

    private int color;
    private int previousColor;
    // TODO add alpha

    private final InterpolatedLinearChasingValue alpha;

    public ColorInfo() {
        this.alpha = new InterpolatedLinearChasingValue().withStep(1/30F).start(1);
        start(0xFFFFFF);
    }

    public void start(int color) {
        this.color = this.previousColor = color;
    }

    public void tick() {
        alpha.tick();
    }

    public int getColor(float partialTicks) {
        float amount = alpha.get(partialTicks);
        return ColorHelper.mixColors(color, previousColor, amount);
    }

    public void setColor(int newColor) {
        previousColor = getColor(0);
        color = newColor;
        alpha.set(0);
    }

    public int getTargetColor() {
        return color;
    }
}
