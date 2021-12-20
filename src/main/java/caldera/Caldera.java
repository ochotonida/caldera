package caldera;

import caldera.common.brew.BrewTypeManager;
import caldera.common.brew.generic.component.trigger.TriggerEvents;
import caldera.common.init.*;
import caldera.common.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Caldera.MODID)
public class Caldera {

    public static final String MODID = "caldera";

    public static final Logger LOGGER = LogManager.getLogger("Caldera");

    public Caldera() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> CalderaClient::new);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(CalderaRegistries::onNewRegistry);

        new TriggerEvents();

        MinecraftForge.EVENT_BUS.addListener(BrewTypeManager::onAddReloadListener);
        MinecraftForge.EVENT_BUS.addListener(BrewTypeManager::onDataPackReload);

        ModBlocks.REGISTRY.register(modEventBus);
        ModBlockEntityTypes.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModRecipeTypes.REGISTRY.register(modEventBus);
        ModSoundEvents.REGISTRY.register(modEventBus);
        ModParticleTypes.REGISTRY.register(modEventBus);
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }
}
