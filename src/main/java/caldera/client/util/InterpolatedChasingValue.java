/*
 * MIT License
 *
 * Copyright (c) 2020 simibubi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * https://github.com/Creators-of-Create/Create/blob/3d825fe632c54e34772247863083ef70a8e6555c/src/main/java/com/simibubi/create/foundation/gui/widgets/InterpolatedChasingValue.java
 */

package caldera.client.util;

public class InterpolatedChasingValue extends InterpolatedValue {

    float speed = 0.5f;
    float target = 0;
    float eps = 1 / 4096f;

    public void tick() {
        float diff = getCurrentDiff();
        if (Math.abs(diff) < eps)
            return;
        set(value + (diff) * speed);
    }

    protected float getCurrentDiff() {
        return getTarget() - value;
    }

    public InterpolatedChasingValue withSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public InterpolatedChasingValue target(float target) {
        this.target = target;
        return this;
    }

    public InterpolatedChasingValue start(float value) {
        lastValue = this.value = value;
        target(value);
        return this;
    }

    public float getTarget() {
        return target;
    }
}
