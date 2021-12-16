package caldera.common.brew.generic.component.effect;

import caldera.common.brew.BrewTypeDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface EffectProviderType<PROVIDER extends EffectProvider> extends IForgeRegistryEntry<EffectProviderType<?>> {

    PROVIDER deserialize(JsonObject object, BrewTypeDeserializationContext context);

    PROVIDER deserialize(FriendlyByteBuf buffer);
}
