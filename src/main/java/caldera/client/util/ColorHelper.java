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

    public static int mixColors(int color1, int color2, float a) {
        int r = (int) Math.sqrt((Math.pow(getRed(color1), 2) * (1 - a) + Math.pow(getRed(color2), 2) * a));
        int g = (int) Math.sqrt((Math.pow(getGreen(color1), 2) * (1 - a) + Math.pow(getGreen(color2), 2) * a));
        int b = (int) Math.sqrt((Math.pow(getBlue(color1), 2) * (1 - a) + Math.pow(getBlue(color2), 2) * a));
        int alpha = (int) (getAlpha(color1) * (1 - a) + getAlpha(color2) * a);

        return fromRGBA(r, g, b, alpha);
    }

    public static int applyAlpha(int color, float alpha) {
        int previousAlpha = getAlpha(color);
        alpha *= 256;
        if (previousAlpha > 0) {
            alpha = alpha * previousAlpha / 0xFF;
        }
        return (color & 0xFFFFFF) | MathHelper.clamp((int) alpha, 0x00, 0xFF) << 24;
    }

    public static int fromRGBA(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | fromRGB(r, g, b);
    }

    public static int fromRGB(int r, int g, int b) {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
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
