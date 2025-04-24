package caldera.datagen;

import caldera.Caldera;
import caldera.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemModels extends ItemModelProvider {

    public ItemModels(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Caldera.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        addGeneratedModel(ModBlocks.LARGE_CAULDRON.get());
    }

    private void addGeneratedModel(ItemLike item) {
        // noinspection ConstantConditions
        String name = BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
        ResourceLocation texture = Caldera.id("item/" + name);
        withExistingParent("item/" + name, "item/generated").texture("layer0", texture);
    }
}
