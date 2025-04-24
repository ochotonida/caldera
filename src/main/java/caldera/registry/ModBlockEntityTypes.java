package caldera.registry;

import caldera.Caldera;
import caldera.block.cauldron.CauldronBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Caldera.MOD_ID);

    @SuppressWarnings("ConstantConditions")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CauldronBlockEntity>> LARGE_CAULDRON = ENTITY_TYPES.register("cauldron", () -> BlockEntityType.Builder.of(CauldronBlockEntity::new, ModBlocks.LARGE_CAULDRON.get()).build(null));
}
