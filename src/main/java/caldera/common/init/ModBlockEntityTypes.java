package caldera.common.init;

import caldera.Caldera;
import caldera.common.block.cauldron.CauldronBlockEntity;
import caldera.common.block.cauldron.CauldronBlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Caldera.MODID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<CauldronBlockEntity>> LARGE_CAULDRON = REGISTRY.register("cauldron", () -> BlockEntityType.Builder.of(CauldronBlockEntity::new, ModBlocks.LARGE_CAULDRON.get()).build(null));

    public static void addRenderers() {
        ClientRegistry.bindTileEntityRenderer(LARGE_CAULDRON.get(), CauldronBlockEntityRenderer::new);
    }
}
