package caldera.registry;

import caldera.Caldera;
import caldera.block.cauldron.LargeCauldronBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Caldera.MOD_ID);

    public static final DeferredHolder<Block, LargeCauldronBlock> LARGE_CAULDRON = BLOCKS.register("large_cauldron", () -> new LargeCauldronBlock(
            Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(6.5F)
                    .sound(ModSoundTypes.CAULDRON)
                    .requiresCorrectToolForDrops()
    ));
}
