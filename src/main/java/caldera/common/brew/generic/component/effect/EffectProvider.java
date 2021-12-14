package caldera.common.brew.generic.component.effect;

import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public interface EffectProvider {

    Effect create(GenericBrew brew, String identifier);

    Effect loadEffect(GenericBrew brew, CompoundTag tag, String identifier);

    EffectProviderType<?> getType();

    default JsonObject toJson() {
        JsonObject result = new JsonObject();
        //noinspection ConstantConditions
        result.addProperty("effectType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    default void toNetwork(FriendlyByteBuf buffer) {
        // noinspection ConstantConditions
        buffer.writeResourceLocation(getType().getRegistryName());
        serialize(buffer);
    }

    void serialize(JsonObject object);

    void serialize(FriendlyByteBuf buffer);

}
