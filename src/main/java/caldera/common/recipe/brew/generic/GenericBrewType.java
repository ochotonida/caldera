package caldera.common.recipe.brew.generic;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.Cauldron;
import caldera.common.recipe.brew.Brew;
import caldera.common.recipe.brew.BrewType;
import caldera.common.recipe.brew.generic.component.ActionHandler;
import caldera.common.recipe.brew.generic.component.effect.EffectProvider;
import caldera.common.recipe.brew.generic.component.effect.EffectProviders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.HashMap;
import java.util.Map;

public class GenericBrewType extends BrewType {

    private final ActionHandler actions = ActionHandler.create();
    private final Map<String, EffectProvider> effects;

    protected GenericBrewType(ResourceLocation id, Map<String, EffectProvider> effects) {
        super(id);
        this.effects = effects;
    }

    public ActionHandler getActions() {
        return actions;
    }

    public Map<String, EffectProvider> getEffects() {
        return effects;
    }

    @Override
    public Brew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return new GenericBrew(this, cauldron);
    }

    @Override
    public Brew create(Cauldron cauldron) {
        return new GenericBrew(this, cauldron);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.GENERIC_BREW_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<GenericBrewType> {

        @Override
        public GenericBrewType fromJson(ResourceLocation id, JsonObject object) {
            Map<String, EffectProvider> effects = deserializeEffects(GsonHelper.getAsJsonObject(object, "effects"));
            return new GenericBrewType(id, effects);
        }

        @Override
        public GenericBrewType fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Map<String, EffectProvider> effects = deserializeEffects(buffer);
            return new GenericBrewType(id, effects);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericBrewType brewType) {
            serializeEffects(buffer, brewType);
        }

        public static Map<String, EffectProvider> deserializeEffects(JsonObject object) {
            HashMap<String, EffectProvider> result = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                String identifier = entry.getKey();
                if (!entry.getValue().isJsonObject()) {
                    throw new JsonParseException(String.format("Expected value for effect '%s' to be an object, was '%s'", identifier, entry.getValue()));
                }

                EffectProvider provider = EffectProviders.EFFECTS.fromJson(entry.getValue().getAsJsonObject());
                result.put(identifier, provider);
            }

            return result;
        }

        public static Map<String, EffectProvider> deserializeEffects(FriendlyByteBuf buffer) {
            HashMap<String, EffectProvider> effectProviders = new HashMap<>();

            while (buffer.readBoolean()) {
                String identifier = buffer.readUtf();
                EffectProvider provider = EffectProviders.EFFECTS.fromNetwork(buffer);
                effectProviders.put(identifier, provider);
            }

            return effectProviders;
        }

        public void serializeEffects(FriendlyByteBuf buffer, GenericBrewType brewType) {
            brewType.effects.forEach((identifier, effectProvider) -> {
                buffer.writeBoolean(true);
                effectProvider.toNetwork(buffer);
            });
            buffer.writeBoolean(false);
        }
    }
}
