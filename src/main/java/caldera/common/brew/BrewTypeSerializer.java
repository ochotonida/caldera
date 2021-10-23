package caldera.common.brew;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface BrewTypeSerializer<TYPE extends BrewType> extends IForgeRegistryEntry<BrewTypeSerializer<?>> {

    TYPE fromJson(JsonObject object, BrewTypeDeserializationContext context);

    TYPE fromNetwork(ResourceLocation brewTypeId, FriendlyByteBuf buffer);

    void toNetwork(FriendlyByteBuf buffer, TYPE brewType);
}
