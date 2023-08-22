package caldera.common.brew.generic.component.action;

import caldera.common.brew.BrewTypeDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ActionType<ACTION extends SimpleAction> extends IForgeRegistryEntry<ActionType<?>> {

    ACTION deserialize(JsonObject object, BrewTypeDeserializationContext context);
}
