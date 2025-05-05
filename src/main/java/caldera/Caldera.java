package caldera;

import caldera.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Caldera.MOD_ID)
public class Caldera {

    public static final String MOD_ID = "caldera";

    public Caldera(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            new CalderaClient(modBus);
        }

        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntityTypes.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSoundEvents.SOUND_EVENTS.register(modBus);

        modBus.addListener(ModCapabilities::registerCapabilities);

        modBus.addListener(CalderaData::gatherData);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Caldera.MOD_ID, path);
    }
}
