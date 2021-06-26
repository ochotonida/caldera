package caldera.common.init;

import caldera.Caldera;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class ModTags {

    public static final ITag.INamedTag<Item> INERT = itemTag("inert");

    public static ITag.INamedTag<Item> itemTag(String path) {
        return ItemTags.createOptional(new ResourceLocation(Caldera.MODID, path));
    }

    public static ITag.INamedTag<Item> itemTag(String modid, String path) {
        return ItemTags.createOptional(new ResourceLocation(modid, path));
    }
}
