package caldera;

import caldera.block.cauldron.CauldronBlockEntityRenderer;
import caldera.registry.ModBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class CalderaClient {

    public CalderaClient(IEventBus modBus) {
        modBus.addListener(this::onClientSetup);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(ModBlockEntityTypes.LARGE_CAULDRON.get(), CauldronBlockEntityRenderer::new));
    }
}
