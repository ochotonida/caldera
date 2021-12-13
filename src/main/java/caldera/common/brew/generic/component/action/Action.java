package caldera.common.brew.generic.component.action;

import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public interface Action extends Consumer<GenericBrew> {

    ActionType<?> getType();

    default JsonObject toJson() {
        JsonObject result = new JsonObject();
        // noinspection ConstantConditions
        result.addProperty("actionType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    default void toNetwork(FriendlyByteBuf buffer) {
        //noinspection ConstantConditions
        buffer.writeResourceLocation(getType().getRegistryName());
        serialize(buffer);
    }

    void serialize(JsonObject object);

    void serialize(FriendlyByteBuf buffer);
}
