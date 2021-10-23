package caldera.common.network;

import caldera.common.brew.BrewType;
import caldera.common.brew.BrewTypeManager;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.init.CalderaRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BrewTypeSyncPacket {

    private final Map<ResourceLocation, BrewType> brewTypes;

    @SuppressWarnings("unused")
    public BrewTypeSyncPacket(FriendlyByteBuf buffer) {
        brewTypes = new HashMap<>();
        while (buffer.readBoolean()) {
            ResourceLocation serializerId = buffer.readResourceLocation();
            ResourceLocation id = buffer.readResourceLocation();

            if (!CalderaRegistries.BREW_TYPE_SERIALIZERS.containsKey(serializerId)) {
                throw new IllegalArgumentException("Unknown brew type serializer " + serializerId);
            }

            BrewTypeSerializer<?> serializer = CalderaRegistries.BREW_TYPE_SERIALIZERS.getValue(serializerId);
            // noinspection ConstantConditions
            BrewType brewType = serializer.fromNetwork(id, buffer);
            brewTypes.put(brewType.getId(), brewType);
        }
    }

    public BrewTypeSyncPacket(Map<ResourceLocation, BrewType> brewTypes) {
        this.brewTypes = brewTypes;
    }

    @SuppressWarnings("unused")
    void encode(FriendlyByteBuf buffer) {
        brewTypes.values().forEach(brewType -> {
            buffer.writeBoolean(true);
            // noinspection ConstantConditions
            buffer.writeResourceLocation(brewType.getSerializer().getRegistryName());
            buffer.writeResourceLocation(brewType.getId());
            putBrewType(buffer, brewType);
        });
        buffer.writeBoolean(false);
    }

    private <TYPE extends BrewType> void putBrewType(FriendlyByteBuf buffer, TYPE brewType) {
        // noinspection unchecked
        ((BrewTypeSerializer<TYPE>) brewType.getSerializer()).toNetwork(buffer, brewType);
    }

    void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> BrewTypeManager.setBrewTypes(brewTypes));
        context.get().setPacketHandled(true);
    }
}
