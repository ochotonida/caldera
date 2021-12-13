package caldera.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.Locale;

public class JsonHelper {

    public static <ENUM extends Enum<ENUM>> ENUM getAsEnumValue(JsonObject object, String memberName, Class<ENUM> enumClass) {
        ENUM value;
        try {
            value = Enum.valueOf(enumClass, GsonHelper.getAsString(object, memberName).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new JsonParseException("Invalid value for %s, expected one of the following values: %s".formatted(memberName, Arrays.toString(Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).map(String::toLowerCase).toArray())));
        }
        return value;
    }

    public static JsonElement writeEnumValue(Enum<?> value) {
        return new JsonPrimitive(value.name().toLowerCase(Locale.ROOT));
    }
}
