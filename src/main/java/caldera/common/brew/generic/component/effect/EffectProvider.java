package caldera.common.brew.generic.component.effect;

import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        //noinspection ConstantConditions
        result.addProperty("effectType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        // noinspection ConstantConditions
        buffer.writeResourceLocation(getType().getRegistryName());
        serialize(buffer);
    }

    public abstract void serialize(JsonObject object);

    public abstract void serialize(FriendlyByteBuf buffer);

}
