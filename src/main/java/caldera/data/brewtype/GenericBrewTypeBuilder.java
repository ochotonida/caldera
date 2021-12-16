package caldera.data.brewtype;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.GroupAction;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.effects.TimerEffectType;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.triggers.EffectEndedTriggerType;
import caldera.common.init.ModBrewTypes;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class GenericBrewTypeBuilder {

    private final ResourceLocation id;
    private final Map<String, Action> actions = new LinkedHashMap<>();
    private final Map<String, GroupAction> groups = new LinkedHashMap<>();
    private final Map<String, EffectProvider> effects = new LinkedHashMap<>();
    private final Map<Trigger, String> triggers = new LinkedHashMap<>();

    private GenericBrewTypeBuilder(ResourceLocation id) {
        this.id = id;
    }

    public GenericBrewTypeBuilder addAction(String identifier, SimpleAction action) {
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

    public GroupBuilder onTrigger(Trigger trigger) {
        return new GroupBuilder().onTrigger(trigger);
    }

    public GroupBuilder onEffectEnded(String timerIdentifier) {
        return onTrigger(EffectEndedTriggerType.effectEnded(timerIdentifier));
    }

    public static GenericBrewTypeBuilder builder(String id) {
        return new GenericBrewTypeBuilder(new ResourceLocation(Caldera.MODID, id));
    }

    public void save(Consumer<FinishedBrewType> brewTypeConsumer) {
        actions.putAll(groups);

        validateEffects();
        validateGroups();
        validateActions();
        validateTriggers();

        brewTypeConsumer.accept(new Result(id, actions, effects, triggers));
    }

    private void validateEffects() {
        effects.keySet().forEach(identifier -> {
            if (!JsonHelper.isValidIdentifier(identifier)) {
                throw new IllegalStateException("Non [a-z0-9_-] character in effect identifier: " + identifier);
            }
        });
    }

    private void validateActions() {
        actions.forEach((identifier, _action) -> {
            if (!JsonHelper.isValidIdentifier(identifier)) {
                throw new IllegalStateException("Non [a-z0-9_-] character in action identifier: " + identifier);
            }
        });
    }

    private void validateGroups() {
        List<String> cycle = GroupAction.findCycle(groups);
        if (!cycle.isEmpty()) {
            throw new IllegalStateException("Cyclical group definition: " + GroupAction.formatCycle(cycle));
        }
    }

    private void validateTriggers() {
        triggers.forEach((trigger, action) -> {
            if (actions.keySet().stream().noneMatch(action::equals)) {
                throw new IllegalStateException("Undefined action '%s'".formatted(action));
            }
        });
    }

    public class GroupBuilder {

        private String identifier;
        private Trigger trigger;
        private final List<String> actions = new ArrayList<>();

        public GroupBuilder groupId(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public GroupBuilder onTrigger(Trigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public GroupBuilder executeAction(String identifier) {
            actions.add(identifier);
            return this;
        }

        public GroupBuilder executeAction(String identifier, SimpleAction action) {
            GenericBrewTypeBuilder.this.addAction(identifier, action);
            executeAction(identifier);
            return this;
        }

        public GroupBuilder startEffect(String identifier, EffectProvider effectProvider) {
            addEffect(identifier, effectProvider);
            return startEffect(identifier);
        }

        public GroupBuilder startEffect(String identifier) {
            return executeAction("start." + identifier);
        }

        public GroupBuilder removeEffect(String identifier) {
            return executeAction("remove." + identifier);
        }

        public GroupBuilder startTimer(String identifier, int timerDuration) {
            return startEffect(identifier, TimerEffectType.timer(timerDuration));
        }

        public GenericBrewTypeBuilder end() {
            if (actions.size() == 1) {
                GenericBrewTypeBuilder.this.triggers.put(trigger, actions.get(0));
            } else {
                GroupAction group = new GroupAction(actions);
                GenericBrewTypeBuilder.this.groups.put(identifier, group);
                GenericBrewTypeBuilder.this.triggers.put(trigger, identifier);
            }
            return GenericBrewTypeBuilder.this;
        }
    }

    public record Result(ResourceLocation id, Map<String, Action> actions, Map<String, EffectProvider> effects, Map<Trigger, String> triggers) implements FinishedBrewType {

        public ResourceLocation getId() {
            return this.id();
        }

        public BrewTypeSerializer<?> getType() {
            return ModBrewTypes.GENERIC_BREW_SERIALIZER.get();
        }

        public void serializeBrewTypeData(JsonObject object) {
            object.add("effects", serializeEffects());
            object.add("actions", serializeActions());
            object.add("triggers", serializeTriggers());
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
            triggers().forEach((trigger, action) -> {
                JsonObject object = new JsonObject();
                object.add("trigger", trigger.toJson());
                object.addProperty("action", action);
                result.add(object);
            });
            return result;
        }
    }
}
