package caldera.data.brewtype;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.GenericBrewType;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.effects.TimerEffectType;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.triggers.EffectEndedTriggerType;
import caldera.common.init.ModBrewTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class GenericBrewTypeBuilder {

    private final ResourceLocation id;
    private final Map<String, Action> actions = new HashMap<>();
    private final Map<String, EffectProvider> effects = new HashMap<>();
    private final List<Map.Entry<Trigger, List<String>>> triggers = new ArrayList<>();

    private GenericBrewTypeBuilder(ResourceLocation id) {
        this.id = id;
    }

    public GenericBrewTypeBuilder addAction(String identifier, Action action) {
        if (actions.containsKey(identifier)) {
            throw new IllegalArgumentException("Action with identifier '%s' already exists".formatted(identifier));
        }
        actions.put(identifier, action);
        return this;
    }

    public GenericBrewTypeBuilder addEffect(String identifier, EffectProvider effectProvider) {
        if (effects.containsKey(identifier)) {
            throw new IllegalArgumentException("Effect with identifier '%s' already exists".formatted(identifier));
        }
        effects.put(identifier, effectProvider);
        return this;
    }

    public EventBuilder onTrigger(Trigger event) {
        return new EventBuilder(event);
    }

    public EventBuilder onEffectEnded(String timerIdentifier) {
        return onTrigger(EffectEndedTriggerType.effectEnded(timerIdentifier));
    }

    public static GenericBrewTypeBuilder builder(String id) {
        return new GenericBrewTypeBuilder(new ResourceLocation(Caldera.MODID, id));
    }

    public void save(Consumer<FinishedBrewType> brewTypeConsumer) {
        validateActions();
        validateEffects();
        validateTriggers();

        brewTypeConsumer.accept(new Result(id, actions, effects, triggers));
    }

    private void validateActions() {
        actions.forEach((identifier, _action) -> {
            if (!GenericBrewType.Serializer.isValidIdentifier(identifier)) {
                throw new IllegalStateException("Non [a-z0-9_-] character in action identifier: " + identifier);
            }
            if (triggers.stream().noneMatch(entry -> entry.getValue().contains(identifier))) {
                throw new IllegalStateException("Action '%s' is never used".formatted(identifier));
            }
        });
    }

    private void validateEffects() {
        effects.keySet().forEach(identifier -> {
            if (!GenericBrewType.Serializer.isValidIdentifier(identifier)) {
                throw new IllegalStateException("Non [a-z0-9_-] character in effect identifier: " + identifier);
            }
        });
    }

    private void validateTriggers() {
        triggers.forEach(entry -> entry.getValue()
                .stream()
                .filter(identifier -> !identifier.endsWith(".start") && !identifier.endsWith(".remove"))
                .forEach(identifier -> {
                    if (actions.keySet().stream().noneMatch(identifier::equals)) {
                        throw new IllegalStateException("Undefined action '%s'".formatted(identifier));
                    }
                })
        );
    }

    public class EventBuilder {

        private final Trigger trigger;
        private final List<String> actions = new ArrayList<>();

        private EventBuilder(Trigger trigger) {
            this.trigger = trigger;
        }

        public EventBuilder executeAction(String identifier) {
            actions.add(identifier);
            return this;
        }

        public EventBuilder executeAction(String identifier, Action action) {
            GenericBrewTypeBuilder.this.addAction(identifier, action);
            executeAction(identifier);
            return this;
        }

        public EventBuilder startEffect(String identifier, EffectProvider effectProvider) {
            addEffect(identifier, effectProvider);
            return executeAction(identifier + ".start");
        }

        public EventBuilder removeEffect(String identifier) {
            return executeAction(identifier + ".remove");
        }

        public EventBuilder startTimer(String identifier, int timerDuration) {
            return startEffect(identifier, TimerEffectType.timer(timerDuration));
        }

        public GenericBrewTypeBuilder end() {
            GenericBrewTypeBuilder.this.triggers.add(Map.entry(trigger, actions));
            return GenericBrewTypeBuilder.this;
        }
    }

    public record Result(
            ResourceLocation id,
            Map<String, Action> actions,
            Map<String, EffectProvider> effects,
            List<Map.Entry<Trigger, List<String>>> triggers
    ) implements FinishedBrewType {

        public ResourceLocation getId() {
            return this.id();
        }

        public BrewTypeSerializer<?> getType() {
            return ModBrewTypes.GENERIC_BREW_SERIALIZER.get();
        }

        public void serializeBrewTypeData(JsonObject object) {
            object.add("actions", serializeActions());
            object.add("effects", serializeEffects());
            object.add("events", serializeTriggers());
        }

        private JsonElement serializeActions() {
            JsonObject result = new JsonObject();
            actions().forEach((identifier, action) -> result.add(identifier, action.toJson()));
            return result;
        }

        private JsonElement serializeEffects() {
            JsonObject result = new JsonObject();
            effects().forEach((identifier, effect) -> result.add(identifier, effect.toJson()));
            return result;
        }

        private JsonElement serializeTriggers() {
            JsonArray result = new JsonArray();
            triggers().forEach(entry -> {
                JsonObject object = new JsonObject();
                object.add("trigger", entry.getKey().toJson());
                JsonArray actionArray = new JsonArray();
                entry.getValue().forEach(actionArray::add);
                object.add("actions", actionArray);
                result.add(object);
            });
            return result;
        }
    }
}
