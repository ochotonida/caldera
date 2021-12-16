package caldera.common.brew.generic.component.action;

import caldera.common.brew.BrewTypeDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ActionType<ACTION extends SimpleAction> extends IForgeRegistryEntry<ActionType<?>> {

    default boolean shouldSendToClients() {
        return false;
    }

    ACTION deserialize(JsonObject object, BrewTypeDeserializationContext context);

    ACTION deserialize(FriendlyByteBuf buffer);
}
