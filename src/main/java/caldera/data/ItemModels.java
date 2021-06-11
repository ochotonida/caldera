package caldera.data;

import caldera.Caldera;
import caldera.common.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {

    public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, Caldera.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        addGeneratedModel(ModBlocks.LARGE_CAULDRON.get());
    }

    private void addGeneratedModel(IItemProvider item) {
        // noinspection ConstantConditions
        String name = item.asItem().getRegistryName().getPath();
        ResourceLocation texture = new ResourceLocation(Caldera.MODID, "item/" + name);
        withExistingParent("item/" + name, "item/generated").texture("layer0", texture);
    }
}
