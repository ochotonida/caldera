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
import caldera.common.init.ModBrewTypes;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Collections;
import java.util.Map;

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
            Map<String, EffectProvider> effects = EffectProvider.fromJson(GsonHelper.getAsJsonObject(object, "effects"));
            Map<String, Action> actions = Action.fromJson(GsonHelper.getAsJsonObject(object, "actions"), effects.keySet());
            Map<TriggerType<?>, TriggerHandler<?>> triggers = TriggerHandler.fromJson(GsonHelper.getAsJsonArray(object, "events"), actions.keySet());
            return new GenericBrewType(context.getBrewType(), actions, effects, triggers);
        }

        @Override
        public GenericBrewType fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Map<String, EffectProvider> effects = EffectProvider.fromNetwork(buffer);
            Map<String, Action> actions = Action.fromNetwork(buffer, effects.keySet());
            return new GenericBrewType(id, actions, effects, Collections.emptyMap());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericBrewType brewType) {
            EffectProvider.toNetwork(buffer, brewType.effects);
            Action.toNetwork(buffer, brewType.actions);
        }
    }
}
