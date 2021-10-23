package caldera.common.brew.sludge;

import caldera.common.brew.BrewType;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.init.ModBrewTypes;
import caldera.common.recipe.Cauldron;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

public record SludgeBrewType(ResourceLocation id, int color) implements BrewType {

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public SludgeBrew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return new SludgeBrew(this, cauldron, color);
    }

    @Override
    public SludgeBrew create(Cauldron cauldron) {
        return new SludgeBrew(this, cauldron, color);
    }

    @Override
    public BrewTypeSerializer<?> getSerializer() {
        return ModBrewTypes.SLUDGE_BREW_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<BrewTypeSerializer<?>> implements BrewTypeSerializer<SludgeBrewType> {

        @Override
        public SludgeBrewType fromJson(JsonObject object, BrewTypeDeserializationContext context) {
            int color = ColorHelper.readColor(object, "color");
            return new SludgeBrewType(context.getBrewType(), color);
        }

        @Override
        public SludgeBrewType fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int color = buffer.readInt();
            return new SludgeBrewType(id, color);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SludgeBrewType type) {
            buffer.writeInt(type.color);
        }
    }
}
