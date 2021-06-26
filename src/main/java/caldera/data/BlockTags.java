package caldera.data;

import caldera.Caldera;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTags extends BlockTagsProvider {

    public BlockTags(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, Caldera.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {

    }
}
