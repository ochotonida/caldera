package caldera.common.init;

import caldera.Caldera;
import caldera.common.block.cauldron.LargeCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {

    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, Caldera.MODID);

    public static final RegistryObject<LargeCauldronBlock> LARGE_CAULDRON = REGISTRY.register("large_cauldron", () -> new LargeCauldronBlock(
            Block.Properties.of(Material.HEAVY_METAL)
                    .strength(6.5F)
                    .sound(ModSoundTypes.CAULDRON)
                    .requiresCorrectToolForDrops()
    ));
}
