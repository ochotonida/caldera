package caldera.data.brewtype;

import caldera.common.brew.BrewTypeSerializer;
import caldera.common.init.CalderaRegistries;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface FinishedBrewType {

    void serializeBrewTypeData(JsonObject object);

    ResourceLocation getId();

    BrewTypeSerializer<?> getType();

    default JsonObject serializeBrewType() {
        JsonObject jsonobject = new JsonObject();
        // noinspection ConstantConditions
        jsonobject.addProperty("type", CalderaRegistries.BREW_TYPE_SERIALIZERS.getKey(getType()).toString());
        this.serializeBrewTypeData(jsonobject);
        return jsonobject;
    }
}
