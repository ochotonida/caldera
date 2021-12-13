package caldera;

import caldera.common.block.cauldron.CauldronBlockEntityRenderer;
import caldera.common.init.ModBlockEntityTypes;
import caldera.common.init.ModParticleTypes;
import caldera.common.particle.CauldronBubbleParticle;
import caldera.common.particle.CauldronSplashParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
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
        event.enqueueWork(() -> BlockEntityRenderers.register(ModBlockEntityTypes.LARGE_CAULDRON.get(), CauldronBlockEntityRenderer::new));
    }

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            event.addSprite(new ResourceLocation(Caldera.MODID, "block/brew"));
        }
    }

    @SubscribeEvent
    public void onRegisterParticleFactories(ParticleFactoryRegisterEvent event) {
        ParticleEngine manager = Minecraft.getInstance().particleEngine;

        manager.register(ModParticleTypes.CAULDRON_BUBBLE.get(), CauldronBubbleParticle.Factory::new);
        manager.register(ModParticleTypes.CAULDRON_SPLASH.get(), CauldronSplashParticle.Factory::new);
    }
}
