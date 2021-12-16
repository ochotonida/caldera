package caldera.common.brew.generic.component.action;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.init.CalderaRegistries;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public abstract class SimpleAction implements Action {

    private String identifier;

    public abstract void execute(GenericBrew brew);

    public abstract ActionType<?> getType();

    public abstract void serialize(JsonObject object);

    public abstract void serialize(FriendlyByteBuf buffer);

    public String getIdentifier() {
        return identifier;
    }

    private void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public final void accept(GenericBrew brew) {
        execute(brew);
        if (getType().shouldSendToClients()) {
            brew.sendActionExecuted(getIdentifier());
        }
    }

    public final JsonObject toJson() {
        JsonObject result = new JsonObject();
        // noinspection ConstantConditions
        result.addProperty("actionType", getType().getRegistryName().toString());
        serialize(result);
        return result;
    }

    public final void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeUtf(getIdentifier());
        // noinspection ConstantConditions
        buffer.writeResourceLocation(getType().getRegistryName());
        serialize(buffer);
    }

    public static SimpleAction fromJson(String identifier, JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation actionId = new ResourceLocation(GsonHelper.getAsString(object, "actionType"));
        if (!CalderaRegistries.ACTION_TYPES.containsKey(actionId)) {
            throw new JsonParseException("Unknown action type: " + actionId);
        }

        // noinspection ConstantConditions
        SimpleAction action = CalderaRegistries.ACTION_TYPES.getValue(actionId).deserialize(object, context);
        action.setIdentifier(identifier);
        return action;
    }

    public static SimpleAction fromNetwork(FriendlyByteBuf buffer) {
        String identifier = buffer.readUtf();
        ResourceLocation actionId = buffer.readResourceLocation();
        // noinspection ConstantConditions
        SimpleAction result =  CalderaRegistries.ACTION_TYPES.getValue(actionId).deserialize(buffer);
        result.setIdentifier(identifier);
        return result;
    }
}
