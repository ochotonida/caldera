package caldera.common.brew.generic.component.action;

import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record EffectAction(String effect, Type type) implements Action {

    public String createIdentifier() {
        return type().addPrefix(effect());
    }

    @Override
    public void accept(GenericBrew genericBrew) {
        switch (type) {
            case START -> genericBrew.startEffect(effect);
            case REMOVE -> genericBrew.removeEffect(effect);
        }
        genericBrew.sendActionExecuted(createIdentifier());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(createIdentifier());
    }

    public static boolean isEffectAction(String action, Set<String> existingEffects) {
        for (Type type : Type.values()) {
            if (action.startsWith(type.prefix)) {
                String effect = action.substring(type.prefix.length());
                return existingEffects.contains(effect);
            }
        }
        return false;
    }

    public static Map<String, Action> createEffectActions(Set<String> effects) {
        Map<String, Action> result = new HashMap<>();
        for (String effect : effects) {
            for (Type type : Type.values()) {
                String identifier = type.addPrefix(effect);
                Action action = new EffectAction(effect, type);
                result.put(identifier, action);
            }
        }
        return result;
    }

    public static String remove(String effect) {
        return Type.REMOVE.prefix + effect;
    }

    private enum Type {
        START("start."),
        REMOVE("remove.");

        private final String prefix;

        Type(String prefix) {
            this.prefix = prefix;
        }

        private String addPrefix(String effect) {
            return prefix + effect;
        }
    }
}
