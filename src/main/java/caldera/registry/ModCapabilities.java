package caldera.registry;

import caldera.block.cauldron.CauldronBlockEntity;
import caldera.block.cauldron.LargeCauldronBlock;
import caldera.block.multiblock.MultiblockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ModCapabilities {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerSidedCauldronCapability(event, Capabilities.ItemHandler.BLOCK, CauldronBlockEntity::getItemHandler);
        registerSidedCauldronCapability(event, Capabilities.FluidHandler.BLOCK, CauldronBlockEntity::getFluidHandler);
    }

    private static <T> void registerSidedCauldronCapability(
            RegisterCapabilitiesEvent event, BlockCapability<T, Direction> capability, Function<CauldronBlockEntity, T> provider
    ) {
        registerMultiblockEntityCapability(event, ModBlockEntityTypes.LARGE_CAULDRON.get(), capability, (level, pos, state, blockEntity, side) -> {
            if (!state.getValue(LargeCauldronBlock.OCTANT).isLower() && side == Direction.UP) {
                return null;
            } else if (blockEntity instanceof CauldronBlockEntity cauldronBlockEntity) {
                return provider.apply(cauldronBlockEntity);
            }
            return null;
        });
    }

    private static <BE extends MultiblockEntity<BE>, T, C extends @Nullable Object> void registerMultiblockEntityCapability(
            RegisterCapabilitiesEvent event, BlockEntityType<?> blockEntityType, BlockCapability<T, C> capability,
            IBlockCapabilityProvider<T, C> provider
    ) {
        IBlockCapabilityProvider<T, C> adaptedProvider = (level, pos, state, blockEntity, context) -> {
            if (blockEntity != null && blockEntity.getType() == blockEntityType) {
                @SuppressWarnings("unchecked")
                BE multiblockEntity = (BE) blockEntity;
                BE controller = multiblockEntity.getController();
                if (controller != null) {
                    return provider.getCapability(level, pos, state, controller, context);
                }
            }
            return null;
        };

        for (Block block : blockEntityType.getValidBlocks()) {
            event.registerBlock(capability, adaptedProvider, block);
        }
    }
}
