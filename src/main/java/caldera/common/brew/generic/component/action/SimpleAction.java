package caldera.common.brew.generic.component.action;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.init.CalderaRegistries;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public abstract class SimpleAction implements Action {

    private String identifier;

    public abstract ActionType<?> getType();

    public abstract void serialize(JsonObject object);

    public String getIdentifier() {
        return identifier;
    }

    private void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public final JsonObject toJson() {
        JsonObject result = new JsonObject();
        // noinspection ConstantConditions
        result.addProperty("actionType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    public static SimpleAction fromJson(String identifier, JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation actionId = new ResourceLocation(GsonHelper.getAsString(object, "actionType"));
        if (!CalderaRegistries.ACTION_TYPES.containsKey(actionId)) {
            throw new JsonParseException("Unknown action type: " + actionId);
        }

        ActionType<?> actionType = CalderaRegistries.ACTION_TYPES.getValue(actionId);
        // noinspection ConstantConditions
        SimpleAction action = actionType.deserialize(object, context);

        if (action.getType() != actionType) {
            throw new JsonParseException("Action type mismatch, action deserialized using type %s has type %s".formatted(actionType.getRegistryName(), action.getType().getRegistryName()));
        }

        action.setIdentifier(identifier);
        return action;
    }
}
