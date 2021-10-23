package caldera.common.brew.generic.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;

public class GenericBrewTypeComponentRegistry<INSTANCE extends GenericBrewTypeComponent.Instance, TYPE extends GenericBrewTypeComponent<? extends INSTANCE>> implements Iterable<TYPE> {

    private final String name;
    private final HashMap<ResourceLocation, TYPE> types = new HashMap<>();

    public GenericBrewTypeComponentRegistry(String name) {
        this.name = name;
    }

    public <T extends TYPE> T register(T componentType) {
        if (types.containsKey(componentType.getId())) {
            throw new IllegalArgumentException(String.format("Duplicate %s id %s", name, componentType.getId()));
        }
        types.put(componentType.getId(), componentType);

        return componentType;
    }

    @Nullable
    public TYPE get(ResourceLocation id) {
        return types.get(id);
    }

    public INSTANCE fromJson(JsonObject object) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(object, "type"));
        if (!types.containsKey(id)) {
            throw new JsonParseException(String.format("Could not parse unknown %s type %s", name, id));
        }
        TYPE type = types.get(id);
        return type.deserialize(object);
    }

    public INSTANCE fromNetwork(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        TYPE type = types.get(id);
        return type.deserialize(buffer);
    }

    @Override
    public Iterator<TYPE> iterator() {
        return types.values().iterator();
    }
}
