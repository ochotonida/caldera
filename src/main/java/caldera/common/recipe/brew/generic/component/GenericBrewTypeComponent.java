package caldera.common.recipe.brew.generic.component;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface GenericBrewTypeComponent<INSTANCE extends GenericBrewTypeComponent.Instance> {

    ResourceLocation getId();

    INSTANCE deserialize(JsonObject object);

    INSTANCE deserialize(FriendlyByteBuf buffer);

    interface Instance {

        ResourceLocation getType();

        default JsonObject toJson() {
            JsonObject result = new JsonObject();
            result.addProperty("type", getType().toString());
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
}
