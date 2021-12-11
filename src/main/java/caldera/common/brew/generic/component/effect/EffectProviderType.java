package caldera.common.brew.generic.component.effect;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface EffectProviderType<PROVIDER extends EffectProvider> extends IForgeRegistryEntry<EffectProviderType<?>> {

    PROVIDER deserialize(JsonObject object, String identifier);

    PROVIDER deserialize(FriendlyByteBuf buffer, String identifier);
}
