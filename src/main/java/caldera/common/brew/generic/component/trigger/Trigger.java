package caldera.common.brew.generic.component.trigger;

import com.google.gson.JsonObject;

public interface Trigger {

    TriggerType<?> getType();

    default JsonObject toJson() {
        JsonObject result = new JsonObject();
        // noinspection ConstantConditions
        result.addProperty("triggerType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    void serialize(JsonObject object);
}
