package caldera.common.brew.generic.component.trigger;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class TriggerHandler<TRIGGER extends Trigger> {

    private final Map<TRIGGER, String> triggers = new LinkedHashMap<>();
    private final TriggerType<TRIGGER> triggerType;

    public TriggerHandler(TriggerType<TRIGGER> triggerType) {
        this.triggerType = triggerType;
    }

    public void addListener(Trigger trigger, String action) {
        if (trigger.getType() != triggerType) {
            throw new JsonParseException("Trigger has incorrect type %s, expected %s".formatted(trigger.getType().getRegistryName(), triggerType.getRegistryName()));
        }
        // noinspection unchecked
        TRIGGER t = (TRIGGER) trigger;
        if (triggers.containsKey(t)) {
            throw new JsonParseException("Duplicate trigger for type " + trigger.getType().getRegistryName());
        }
        triggers.put(t, action);
    }

    public void trigger(GenericBrew brew, Predicate<TRIGGER> predicate) {
        if (brew.getCauldron().getLevel() == null || brew.getCauldron().getLevel().isClientSide()) {
            return;
        }
        Cauldron cauldron = brew.getCauldron();

        for (Map.Entry<TRIGGER, String> trigger : triggers.entrySet()) {
            if (cauldron.isRemoved() || cauldron.getBrew() != brew) {
                break; // do not execute the remaining actions if a previous action removed the brew
            }
            if (predicate.test(trigger.getKey())) {
                brew.executeAction(trigger.getValue());
            }
        }
    }

    public static Map<TriggerType<?>, TriggerHandler<?>> fromJson(JsonArray array, Set<String> existingActions) {
        HashMap<TriggerType<?>, TriggerHandler<?>> result = new HashMap<>();
        for (JsonElement entry : array) {
            if (!entry.isJsonObject()) {
                throw new JsonParseException("Expected array entry to be an object, was '%s'".formatted(entry));
            }

            Trigger trigger = Trigger.fromJson(GsonHelper.getAsJsonObject(entry.getAsJsonObject(), "trigger"));
            TriggerType<?> triggerType = trigger.getType();

            String action = GsonHelper.getAsString(entry.getAsJsonObject(), "action");
            if (!existingActions.contains(action)) {
                throw new JsonParseException("Action with identifier '%s' is undefined".formatted(action));
            }

            if (!result.containsKey(triggerType)) {
                result.put(triggerType, new TriggerHandler<>(triggerType));
            }
            result.get(triggerType).addListener(trigger, action);
        }

        return result;
    }
}
