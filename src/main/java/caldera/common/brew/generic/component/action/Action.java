package caldera.common.brew.generic.component.action;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface Action extends Consumer<GenericBrew> {


    JsonElement toJson();

    default void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(false);
    }

    static Map<String, Action> fromJson(JsonObject object, Set<String> existingEffects) {
        Map<String, Action> result = new HashMap<>(EffectAction.createEffectActions(existingEffects));
        Set<String> existingActions = new HashSet<>(object.keySet());
        existingActions.addAll(result.keySet());

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String identifier = entry.getKey();
            JsonElement value = entry.getValue();

            JsonHelper.validateIdentifier(identifier, "action_identifier");

            if (!value.isJsonArray() && !value.isJsonObject()) {
                throw new JsonParseException("Expected value for action '%s' to be an object or array, was '%s'".formatted(identifier, value));
            }
        }

        result.putAll(getActions(object));
        result.putAll(getGroups(object, existingEffects, existingActions));
        return result;
    }

    static Map<String, Action> getActions(JsonObject object) throws JsonParseException {
        Map<String, Action> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String identifier = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                result.put(identifier, SimpleAction.fromJson(identifier, value.getAsJsonObject()));
            }
        }

        return result;
    }

    static Map<String, Action> getGroups(JsonObject object, Set<String> effects, Set<String> actions) throws JsonParseException {
        Map<String, GroupAction> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String identifier = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonArray()) {
                result.put(identifier, GroupAction.fromJson(identifier, value.getAsJsonArray(), effects, actions));
            }
        }

        GroupAction.validateGroups(result);
        return new HashMap<>(result);
    }

    static Map<String, Action> fromNetwork(FriendlyByteBuf buffer, Set<String> existingActions) {
        Map<String, Action> result = new HashMap<>(EffectAction.createEffectActions(existingActions));

        while (buffer.readBoolean()) {
            if (buffer.readBoolean()) {
                SimpleAction action = SimpleAction.fromNetwork(buffer);
                result.put(action.getIdentifier(), action);
            }
        }
        return result;
    }

    static void toNetwork(FriendlyByteBuf buffer, Map<String, Action> actions) {
        actions.forEach((identifier, action) -> {
            buffer.writeBoolean(true);
            action.toNetwork(buffer);
        });

        buffer.writeBoolean(false);
    }
}
