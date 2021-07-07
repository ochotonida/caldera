package caldera;

import caldera.common.init.ModBlockEntityTypes;
import caldera.common.init.ModParticleTypes;
import caldera.common.particle.CauldronBubbleParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CalderaClient {

    public CalderaClient() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onTextureStitch);
        modEventBus.addListener(this::onRegisterParticleFactories);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModBlockEntityTypes::addRenderers);
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(PlayerContainer.BLOCK_ATLAS)) {
            event.addSprite(new ResourceLocation(Caldera.MODID, "block/brew"));
        }
    }

    @SubscribeEvent
    public void onRegisterParticleFactories(ParticleFactoryRegisterEvent event) {
        ParticleManager manager = Minecraft.getInstance().particleEngine;

        manager.register(ModParticleTypes.CAULDRON_BUBBLE.get(), CauldronBubbleParticle.Factory::new);
    }
}
