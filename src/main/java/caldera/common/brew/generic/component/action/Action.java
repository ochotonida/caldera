package caldera.common.brew.generic.component.action;

import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface Action extends Consumer<GenericBrew> {

    ResourceLocation getType();

    default JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("actionType", getType().toString());
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
