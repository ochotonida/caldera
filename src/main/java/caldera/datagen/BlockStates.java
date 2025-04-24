package caldera.datagen;

import caldera.Caldera;
import caldera.block.cauldron.LargeCauldronBlock;
import caldera.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockStates extends BlockStateProvider {

    public BlockStates(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Caldera.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile cauldronBottom = models().getExistingFile(Caldera.id(ModelProvider.BLOCK_FOLDER + "/" + "cauldron_bottom"));
        ModelFile cauldronTop = models().getExistingFile(Caldera.id(ModelProvider.BLOCK_FOLDER + "/" + "cauldron_top"));

        getVariantBuilder(ModBlocks.LARGE_CAULDRON.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? cauldronBottom : cauldronTop)
                        .rotationY((int) state.getValue(LargeCauldronBlock.FACING).toYRot())
                        .build());
    }
}
