package caldera.client.util;

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
        int r = JSONUtils.getAsInt(colorObject, "red");
        int g = JSONUtils.getAsInt(colorObject, "green");
        int b = JSONUtils.getAsInt(colorObject, "blue");

        return fromRGB(r, g, b);
    }

    public static JsonObject writeColor(int color) {
        JsonObject object = new JsonObject();
        object.addProperty("red", getRed(color));
        object.addProperty("green", getGreen(color));
        object.addProperty("blue", getBlue(color));
        return object;
    }

    public static int applyAlpha(int color, float alpha) {
        int previousAlpha = getAlpha(color);
        if (previousAlpha > 0) {
            alpha = alpha * 256 * previousAlpha / 0xFF;
        }
        return (color & 0xFFFFFF) | MathHelper.clamp((int) alpha, 0x00, 0xFF) << 24;
    }

    public static int fromRGB(int red, int green, int blue) {
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int getAlpha(int color) {
        return color >> 24 & 0xFF;
    }

    public static int getRed(int color) {
        return color >> 16 & 0xFF;
    }

    public static int getGreen(int color) {
        return color >> 8 & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }
}
