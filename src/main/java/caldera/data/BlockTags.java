package caldera.data;

import caldera.Caldera;
import caldera.common.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTags extends BlockTagsProvider {

    public BlockTags(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, Caldera.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.LARGE_CAULDRON.get());
    }
}
