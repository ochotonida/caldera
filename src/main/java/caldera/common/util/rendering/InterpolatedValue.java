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
 * https://github.com/Creators-of-Create/Create/blob/3d825fe632c54e34772247863083ef70a8e6555c/src/main/java/com/simibubi/create/foundation/gui/widgets/InterpolatedValue.java
 */
package caldera.common.util.rendering;

import net.minecraft.util.math.MathHelper;

public class InterpolatedValue {

    public float value = 0;
    public float lastValue = 0;

    public InterpolatedValue set(float value) {
        lastValue = this.value;
        this.value = value;
        return this;
    }

    public InterpolatedValue init(float value) {
        this.lastValue = this.value = value;
        return this;
    }

    public float get(float partialTicks) {
        return MathHelper.lerp(partialTicks, lastValue, value);
    }

    public boolean settled() {
        return Math.abs(value - lastValue) < 1e-3;
    }
}
