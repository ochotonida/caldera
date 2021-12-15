package caldera.common.brew.generic;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.Brew;
import caldera.common.brew.BrewType;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerHandler;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.init.CalderaRegistries;
import caldera.common.init.ModBrewTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("ClassCanBeRecord")
public class GenericBrewType implements BrewType {

    private final ResourceLocation id;
    private final Map<String, Action> actions;
    private final Map<String, EffectProvider> effects;
    private final Map<TriggerType<?>, TriggerHandler<?>> triggers; // only exists on server

    protected GenericBrewType(ResourceLocation id, Map<String, Action> actions, Map<String, EffectProvider> effects, Map<TriggerType<?>, TriggerHandler<?>> triggers) {
        this.id = id;
        this.actions = actions;
        this.effects = effects;
        this.triggers = triggers;

        effects.forEach((identifier, effect) -> {
            if (!identifier.equals(effect.getIdentifier())) {
                throw new IllegalArgumentException();
            }
        });
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public <INSTANCE extends Trigger> TriggerHandler<INSTANCE> getTrigger(TriggerType<INSTANCE> triggerType) {
        //noinspection unchecked
        return (TriggerHandler<INSTANCE>) triggers.get(triggerType);
    }

    public Map<String, EffectProvider> getEffects() {
        return effects;
    }

    protected Action getAction(String identifier) {
        return actions.get(identifier);
    }

    @Nullable
    public String getEffectFromAction(String actionIdentifier, String suffix) {
        return getEffectFromAction(getEffects().keySet(), actionIdentifier, suffix);
    }

    @Nullable
    private static String getEffectFromAction(Set<String> effects, String actionIdentifier, String suffix) {
        if (actionIdentifier.endsWith(suffix)) {
            String effectId = actionIdentifier.substring(0, actionIdentifier.length() - suffix.length());
            if (effects.contains(effectId)) {
                return effectId;
            }
        }
        return null;
    }


    @Override
    public Brew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return new GenericBrew(this, cauldron);
    }

    @Override
    public Brew create(Cauldron cauldron) {
        return new GenericBrew(this, cauldron);
    }

    @Override
    public BrewTypeSerializer<GenericBrewType> getSerializer() {
        return ModBrewTypes.GENERIC_BREW_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<BrewTypeSerializer<?>> implements BrewTypeSerializer<GenericBrewType> {

        @Override
        public GenericBrewType fromJson(JsonObject object, BrewTypeDeserializationContext context) {
            Map<String, Action> actions = deserializeActions(GsonHelper.getAsJsonObject(object, "actions"));
            Map<String, EffectProvider> effects = deserializeEffects(GsonHelper.getAsJsonObject(object, "effects"));
            Map<TriggerType<?>, TriggerHandler<?>> triggers = deserializeTriggers(GsonHelper.getAsJsonArray(object, "events"), actions.keySet(), effects.keySet());
            return new GenericBrewType(context.getBrewType(), actions, effects, triggers);
        }

        @Override
        public GenericBrewType fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Map<String, Action> actions = deserializeActions(buffer);
            Map<String, EffectProvider> effects = deserializeEffects(buffer);
            return new GenericBrewType(id, actions, effects, Collections.emptyMap());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericBrewType brewType) {
            serializeActions(buffer, brewType);
            serializeEffects(buffer, brewType);
        }

        public static Map<String, Action> deserializeActions(JsonObject object) {
            HashMap<String, Action> result = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String identifier = entry.getKey();
                if (!isValidIdentifier(identifier)) {
                    throw new JsonParseException("Non [a-z0-9_-] character in action identifier: " + identifier);
                }
                if (!entry.getValue().isJsonObject()) {
                    throw new JsonParseException("Expected value for action '%s' to be an object, was '%s'".formatted(identifier, entry.getValue()));
                }
                JsonObject actionObject = entry.getValue().getAsJsonObject();
                ResourceLocation actionId = new ResourceLocation(GsonHelper.getAsString(actionObject, "actionType"));
                if (!CalderaRegistries.ACTION_TYPES.containsKey(actionId)) {
                    throw new JsonParseException("Unknown action type: " + actionId);
                }
                // noinspection ConstantConditions
                Action action = CalderaRegistries.ACTION_TYPES.getValue(actionId).deserialize(actionObject);
                result.put(identifier, action);
            }

            return result;
        }

        public static Map<String, Action> deserializeActions(FriendlyByteBuf buffer) {
            HashMap<String, Action> result = new HashMap<>();

            while (buffer.readBoolean()) {
                String identifier = buffer.readUtf();
                ResourceLocation actionId = buffer.readResourceLocation();
                // noinspection ConstantConditions
                Action action = CalderaRegistries.ACTION_TYPES.getValue(actionId).deserialize(buffer);
                result.put(identifier, action);
            }

            return result;
        }

        public static void serializeActions(FriendlyByteBuf buffer, GenericBrewType brewType) {
            brewType.actions.forEach((identifier, action) -> {
                buffer.writeBoolean(true);
                buffer.writeUtf(identifier);
                action.toNetwork(buffer);
            });

            buffer.writeBoolean(false);
        }

        public static Map<String, EffectProvider> deserializeEffects(JsonObject object) {
            HashMap<String, EffectProvider> result = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String identifier = entry.getKey();
                if (!isValidIdentifier(identifier)) {
                    throw new JsonParseException("Non [a-z0-9_-] character in effect identifier: " + identifier);
                }
                if (!entry.getValue().isJsonObject()) {
                    throw new JsonParseException("Expected value for effect '%s' to be an object, was '%s'".formatted(identifier, entry.getValue()));
                }

                JsonObject providerObject = entry.getValue().getAsJsonObject();
                ResourceLocation providerId = new ResourceLocation(GsonHelper.getAsString(providerObject, "effectType"));
                if (!CalderaRegistries.EFFECT_PROVIDER_TYPES.containsKey(providerId)) {
                    throw new JsonParseException("Unknown effect type: " + providerId);
                }
                // noinspection ConstantConditions
                EffectProvider provider = CalderaRegistries.EFFECT_PROVIDER_TYPES.getValue(providerId).deserialize(providerObject);
                provider.setIdentifier(identifier);
                result.put(identifier, provider);
            }

            return result;
        }

        public static Map<String, EffectProvider> deserializeEffects(FriendlyByteBuf buffer) {
            HashMap<String, EffectProvider> result = new HashMap<>();

            while (buffer.readBoolean()) {
                String identifier = buffer.readUtf();
                ResourceLocation effectProviderId = buffer.readResourceLocation();
                // noinspection ConstantConditions
                EffectProvider provider = CalderaRegistries.EFFECT_PROVIDER_TYPES.getValue(effectProviderId).deserialize(buffer);
                provider.setIdentifier(identifier);
                result.put(identifier, provider);
            }

            return result;
        }

        public static void serializeEffects(FriendlyByteBuf buffer, GenericBrewType brewType) {
            brewType.effects.forEach((identifier, effectProvider) -> {
                buffer.writeBoolean(true);
                buffer.writeUtf(identifier);
                effectProvider.toNetwork(buffer);
            });

            buffer.writeBoolean(false);
        }

        public static Map<TriggerType<?>, TriggerHandler<?>> deserializeTriggers(JsonArray array, Set<String> existingActions, Set<String> existingEffects) {
            HashMap<TriggerType<?>, TriggerHandler<?>> result = new HashMap<>();
            Set<String> usedActions = new HashSet<>();
            for (JsonElement entry : array) {
                if (!entry.isJsonObject()) {
                    throw new JsonParseException("Expected array entry to be an object, was '%s'".formatted(entry));
                }
                JsonObject triggerObject = GsonHelper.getAsJsonObject(entry.getAsJsonObject(), "trigger");
                ResourceLocation triggerTypeId = new ResourceLocation(GsonHelper.getAsString(triggerObject, "triggerType"));
                if (!CalderaRegistries.TRIGGER_TYPES.containsKey(triggerTypeId)) {
                    throw new JsonParseException("Unknown trigger type: " + triggerTypeId);
                }
                TriggerType<?> triggerType = CalderaRegistries.TRIGGER_TYPES.getValue(triggerTypeId);
                // noinspection ConstantConditions
                Trigger trigger = triggerType.deserialize(triggerObject);

                List<String> actions = new ArrayList<>();
                JsonArray actionArray = GsonHelper.getAsJsonArray(entry.getAsJsonObject(), "actions");
                for (JsonElement element : actionArray) {
                    String action = GsonHelper.convertToString(element, "action");
                    if (!existingActions.contains(action)) {
                        if (getEffectFromAction(existingEffects, action, ".start") == null && getEffectFromAction(existingEffects, action, ".remove") == null) {
                            throw new JsonParseException("Action with identifier '%s' is undefined".formatted(action));
                        }
                    }
                    actions.add(action);
                }
                usedActions.addAll(actions);

                if (!result.containsKey(triggerType)) {
                    result.put(triggerType, new TriggerHandler<>(triggerType));
                }
                result.get(triggerType).addListener(trigger, actions);
            }

            Set<String> unusedActions = new HashSet<>(existingActions);
            unusedActions.removeAll(usedActions);
            if (!unusedActions.isEmpty()) {
                throw new JsonParseException("Unused actions: " + unusedActions);
            }

            return result;
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
}
