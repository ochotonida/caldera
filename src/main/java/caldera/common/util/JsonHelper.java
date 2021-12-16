package caldera.common.util;

import com.google.gson.*;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class JsonHelper {

    public static JsonArray toArray(List<?> list) {
        JsonArray result = new JsonArray();
        list.stream().map(JsonHelper::toJsonElement).forEach(result::add);
        return result;
    }

    public static JsonElement toJsonElement(Object object) {
        if (object instanceof JsonElement element) {
            return element;
        } else if (object instanceof String s) {
            return new JsonPrimitive(s);
        } else if (object instanceof Number n) {
            return new JsonPrimitive(n);
        } else if (object instanceof Boolean b) {
            return new JsonPrimitive(b);
        } else if (object instanceof Character c) {
            return new JsonPrimitive(c);
        }
        throw new IllegalArgumentException();
    }

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

    public static void validateIdentifier(String identifier, String memberName) {
        if (!isValidIdentifier(identifier)) {
            throw new JsonParseException("Non [a-z0-9_-] character in %s: %s".formatted(memberName, identifier));
        }
    }

    public static boolean isValidIdentifier(String identifier) {
        for (int i = 0; i < identifier.length(); ++i) {
            if (!isValidIdentifierCharacter(identifier.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValidIdentifierCharacter(char c) {
        return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' ;
    }
}
