package caldera.common.brew.generic.component.action;

import caldera.common.brew.BrewTypeDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public interface ActionType<ACTION extends SimpleAction> extends IForgeRegistryEntry<ActionType<?>> {

    /**
     * Whether actions of this type should be executed on clients.
     * If this method returns false, {@link #deserialize(FriendlyByteBuf)} may return null.
     */
    default boolean shouldSendToClients() {
        return false;
    }

    ACTION deserialize(JsonObject object, BrewTypeDeserializationContext context);

    @Nullable
    ACTION deserialize(FriendlyByteBuf buffer);
}
