package caldera.data.brewtype;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.Actions;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.Triggers;
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
            throw new IllegalArgumentException(String.format("Action with identifier '%s' already exists", identifier));
        }
        actions.put(identifier, action);
        return this;
    }

    public GenericBrewTypeBuilder addEffect(String identifier, EffectProvider effectProvider) {
        if (effects.containsKey(identifier)) {
            throw new IllegalArgumentException(String.format("Effect with identifier '%s' already exists", identifier));
        }
        effects.put(identifier, effectProvider);
        return this;
    }

    public EventBuilder onTrigger(Trigger event) {
        return new EventBuilder(event);
    }

    public EventBuilder onTimer(String timerIdentifier) {
        return onTrigger(Triggers.TIMER.get().timerFinished(timerIdentifier));
    }

    public static GenericBrewTypeBuilder builder(String id) {
        return new GenericBrewTypeBuilder(new ResourceLocation(Caldera.MODID, id));
    }

    public void save(Consumer<FinishedBrewType> brewTypeConsumer) {
        // TODO validate
        brewTypeConsumer.accept(new Result(id, actions, effects, triggers));
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

        public EventBuilder startTimer(String identifier, int timerDuration) {
            addEffect(identifier, EffectProviders.TIMER.get().timer(timerDuration));
            return executeAction("start_" + identifier, Actions.START_EFFECT.get().effect(identifier));
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
