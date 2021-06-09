package caldera.data;

import caldera.Caldera;
import caldera.common.init.ModBlocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStates extends BlockStateProvider {

    public BlockStates(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Caldera.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile cauldronBottom = models().getExistingFile(new ResourceLocation(Caldera.MODID, ModelProvider.BLOCK_FOLDER + "/" + "cauldron_bottom"));
        ModelFile cauldronTop = models().getExistingFile(new ResourceLocation(Caldera.MODID, ModelProvider.BLOCK_FOLDER + "/" + "cauldron_top"));

        getVariantBuilder(ModBlocks.LARGE_CAULDRON.get()).forAllStates(state ->
                ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? cauldronBottom : cauldronTop)
                        .rotationY((int) state.getValue(DoorBlock.FACING).toYRot())
                        .build());
    }
}
