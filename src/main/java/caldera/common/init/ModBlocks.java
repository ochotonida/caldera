package caldera.common.init;

import caldera.Caldera;
import caldera.common.block.LargeCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Caldera.MODID);

    public static final RegistryObject<LargeCauldronBlock> LARGE_CAULDRON = REGISTRY.register("large_cauldron", () -> new LargeCauldronBlock(
            Block.Properties.of(Material.HEAVY_METAL)
                    .strength(1.5F)
                    .harvestTool(ToolType.PICKAXE)
                    .requiresCorrectToolForDrops()
    ));
}
