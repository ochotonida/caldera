package caldera.common.brew.generic.component.effect;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.init.CalderaRegistries;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class EffectProvider {

    private String identifier;

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract Effect create(GenericBrew brew);

    public abstract Effect loadEffect(GenericBrew brew, CompoundTag tag);

    public abstract EffectProviderType<?> getType();

    public final JsonObject toJson() {
        JsonObject result = new JsonObject();
        //noinspection ConstantConditions
        result.addProperty("effectType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    public final void toNetwork(FriendlyByteBuf buffer) {
        // noinspection ConstantConditions
        buffer.writeResourceLocation(getType().getRegistryName());
        serialize(buffer);
    }

    public abstract void serialize(JsonObject object);

    public abstract void serialize(FriendlyByteBuf buffer);

    public static Map<String, EffectProvider> fromJson(JsonObject object) {
        HashMap<String, EffectProvider> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String identifier = entry.getKey();
            JsonHelper.validateIdentifier(identifier, "effect identifier");
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

    public static Map<String, EffectProvider> fromNetwork(FriendlyByteBuf buffer) {
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

    public static void toNetwork(FriendlyByteBuf buffer, Map<String, EffectProvider> effects) {
        effects.forEach((identifier, effectProvider) -> {
            buffer.writeBoolean(true);
            buffer.writeUtf(identifier);
            effectProvider.toNetwork(buffer);
        });

        buffer.writeBoolean(false);
    }
}
