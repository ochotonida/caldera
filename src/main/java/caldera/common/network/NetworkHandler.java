package caldera.common.network;

import caldera.Caldera;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Caldera.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        INSTANCE.registerMessage(0, BrewTypeSyncPacket.class, BrewTypeSyncPacket::encode, BrewTypeSyncPacket::new, BrewTypeSyncPacket::handle);
    }
}
