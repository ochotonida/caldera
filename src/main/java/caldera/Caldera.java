package caldera;

import caldera.common.init.ModBlockEntityTypes;
import caldera.common.init.ModBlocks;
import caldera.common.init.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Caldera.MODID)
public class Caldera {

    public static final String MODID = "caldera";

    public Caldera() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.REGISTRY.register(modEventBus);
        ModBlockEntityTypes.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
    }
}
