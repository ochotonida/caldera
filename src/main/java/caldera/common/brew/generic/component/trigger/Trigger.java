package caldera.common.brew.generic.component.trigger;

import caldera.common.init.CalderaRegistries;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

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

    static Trigger fromJson(JsonObject object) {
        ResourceLocation triggerTypeId = new ResourceLocation(GsonHelper.getAsString(object, "triggerType"));
        if (!CalderaRegistries.TRIGGER_TYPES.containsKey(triggerTypeId)) {
            throw new JsonParseException("Unknown trigger type: " + triggerTypeId);
        }
        TriggerType<?> triggerType = CalderaRegistries.TRIGGER_TYPES.getValue(triggerTypeId);
        // noinspection ConstantConditions
        return triggerType.deserialize(object);
    }
}
