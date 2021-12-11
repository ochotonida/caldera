package caldera.common.brew.generic.component.effect;

import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface EffectProvider {

    Effect create(GenericBrew brew, String identifier);

    Effect loadEffect(GenericBrew brew, CompoundTag tag, String identifier);

    ResourceLocation getType();

    default JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("effectType", getType().toString());
        serialize(result);
        return result;
    }

    default void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(getType());
        serialize(buffer);
    }

    void serialize(JsonObject object);

    void serialize(FriendlyByteBuf buffer);

}
