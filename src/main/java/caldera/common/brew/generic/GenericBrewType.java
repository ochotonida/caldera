package caldera.common.brew.generic;

import caldera.common.brew.Brew;
import caldera.common.brew.BrewType;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.component.ActionHandler;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.init.ModBrewTypes;
import caldera.common.recipe.Cauldron;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;

public class GenericBrewType implements BrewType {

    private final ResourceLocation id;
    private final ActionHandler actions = ActionHandler.create();
    private final Map<String, EffectProvider> effects;

    protected GenericBrewType(ResourceLocation id, Map<String, EffectProvider> effects) {
        this.id = id;
        this.effects = effects;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public ActionHandler getActions() {
        return actions;
    }

    public Map<String, EffectProvider> getEffects() {
        return effects;
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
            Map<String, EffectProvider> effects = deserializeEffects(GsonHelper.getAsJsonObject(object, "effects"));
            return new GenericBrewType(context.getBrewType(), effects);
        }

        @Override
        public GenericBrewType fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Map<String, EffectProvider> effects = deserializeEffects(buffer);
            return new GenericBrewType(id, effects);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericBrewType brewType) {
            serializeEffects(buffer, brewType);
        }

        public static Map<String, EffectProvider> deserializeEffects(JsonObject object) {
            HashMap<String, EffectProvider> result = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String identifier = entry.getKey();
                if (!entry.getValue().isJsonObject()) {
                    throw new JsonParseException(String.format("Expected value for effect '%s' to be an object, was '%s'", identifier, entry.getValue()));
                }

                EffectProvider provider = EffectProviders.EFFECTS.fromJson(entry.getValue().getAsJsonObject());
                result.put(identifier, provider);
            }

            return result;
        }

        public static Map<String, EffectProvider> deserializeEffects(FriendlyByteBuf buffer) {
            HashMap<String, EffectProvider> effectProviders = new HashMap<>();

            while (buffer.readBoolean()) {
                String identifier = buffer.readUtf();
                EffectProvider provider = EffectProviders.EFFECTS.fromNetwork(buffer);
                effectProviders.put(identifier, provider);
            }

            return effectProviders;
        }

        public void serializeEffects(FriendlyByteBuf buffer, GenericBrewType brewType) {
            brewType.effects.forEach((identifier, effectProvider) -> {
                buffer.writeBoolean(true);
                effectProvider.toNetwork(buffer);
            });
            buffer.writeBoolean(false);
        }
    }
}
