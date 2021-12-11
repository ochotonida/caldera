package caldera.common.brew.generic.component.action;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ActionType<ACTION extends Action> extends IForgeRegistryEntry<ActionType<?>> {

    ACTION deserialize(JsonObject object);

    ACTION deserialize(FriendlyByteBuf buffer);
}
