package caldera.common.brew.generic.component.trigger;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface Trigger {

    ResourceLocation getType();

    default JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty("type", getType().toString());
        serialize(result);
        return result;
    }

    void serialize(JsonObject object);
}
