package caldera.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class ChasingValue {

    private float step;
    private float target;
    private float value;
    private float previousValue;

    public ChasingValue(float step, float initialValue) {
        this.step = step;
        this.target = initialValue;
        this.value = initialValue;
        this.previousValue = initialValue;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void setValue(float value) {
        this.value = value;
        this.previousValue = value;
    }

    public void updateValue(float value) {
        this.previousValue = this.value;
        this.value = value;
    }

    public void reset(float value) {
        setValue(value);
        setTarget(value);
    }

    public float getTarget() {
        return target;
    }

    public float getValue() {
        return value;
    }

    public float getValue(float partialTicks) {
        return Mth.lerp(partialTicks, previousValue, value);
    }

    public void tick() {
        float difference = Math.abs(getTarget() - value);
        float step = Math.min(this.step, difference);
        if (value > target) {
            step *= -1;
        }
        updateValue(value + step);
    }

    public CompoundTag save() {
        CompoundTag result = new CompoundTag();
        result.putFloat("Step", step);
        result.putFloat("Value", value);
        result.putFloat("Target", target);
        return result;
    }

    public void load(CompoundTag tag) {
        step = tag.getFloat("Step");
        value = previousValue = tag.getFloat("Value");
        target = tag.getFloat("Target");
    }
}
