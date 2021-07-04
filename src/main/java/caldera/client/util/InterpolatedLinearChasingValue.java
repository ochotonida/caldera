package caldera.client.util;

public class InterpolatedLinearChasingValue extends InterpolatedValue {

    float step = 1;
    float target = 0;

    public void tick() {
        float difference = Math.abs(getTarget() - value);
        float step = Math.min(this.step, difference);
        if (value > target) {
            step *= -1;
        }
        set(value + step);
    }

    public InterpolatedLinearChasingValue withStep(float step) {
        this.step = step;
        return this;
    }

    public InterpolatedLinearChasingValue target(float target) {
        this.target = target;
        return this;
    }

    public InterpolatedLinearChasingValue start(float value) {
        lastValue = this.value = value;
        target(value);
        return this;
    }

    public float getTarget() {
        return target;
    }
}
