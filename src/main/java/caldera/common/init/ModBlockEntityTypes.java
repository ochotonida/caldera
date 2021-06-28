package caldera.common.init;

import caldera.Caldera;
import caldera.common.block.cauldron.CauldronBlockEntity;
import caldera.common.block.cauldron.CauldronBlockEntityRenderer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockEntityTypes {

    public static final DeferredRegister<TileEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Caldera.MODID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<CauldronBlockEntity>> LARGE_CAULDRON = REGISTRY.register("cauldron", () -> TileEntityType.Builder.of(CauldronBlockEntity::new, ModBlocks.LARGE_CAULDRON.get()).build(null));

    public static void addRenderers() {
        ClientRegistry.bindTileEntityRenderer(LARGE_CAULDRON.get(), CauldronBlockEntityRenderer::new);
    }
}