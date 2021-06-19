package caldera;

import caldera.common.init.ModBlockEntityTypes;
import caldera.common.init.ModBlocks;
import caldera.common.init.ModItems;
import caldera.common.init.ModRecipeTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Caldera.MODID)
public class Caldera {

    public static final String MODID = "caldera";

    public Caldera() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.REGISTRY.register(modEventBus);
        ModBlockEntityTypes.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModRecipeTypes.REGISTRY.register(modEventBus);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(ModBlockEntityTypes::addRenderers);
        }
    }
}
