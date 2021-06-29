package caldera.common.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;

public class ColorHelper {

    public static int readColor(JsonObject object, String name) {
        if (!object.has(name)) {
            throw new JsonSyntaxException("Missing " + name + ", expected to find color");
        }
        JsonObject colorObject = JSONUtils.convertToJsonObject(object.get(name), name);
        int red = JSONUtils.getAsInt(colorObject, "red");
        int green = JSONUtils.getAsInt(colorObject, "green");
        int blue = JSONUtils.getAsInt(colorObject, "blue");

        return fromRGB(red, green, blue);
    }

    public static JsonObject writeColor(int color) {
        JsonObject object = new JsonObject();
        int[] colors = toRGB(color);
        object.addProperty("red", colors[0]);
        object.addProperty("green", colors[1]);
        object.addProperty("blue", colors[2]);
        return object;
    }

    public static int mixAlpha(int color, float alpha) {
        int a = (int) (alpha * 256 * (color >> 24 & 0xFF) / 0xFF);
        a = MathHelper.clamp(a, 0, 0xFF);
        return (color & 0xFFFFFF) | a << 24;

    }

    public static int fromRGB(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int[] toRGB(int color) {
        return new int[] {
                color >> 16 & 0xFF,
                color >> 8 & 0xFF,
                color & 0xFF
        };
    }
}
