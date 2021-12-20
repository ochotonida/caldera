package caldera.common.init;

import caldera.Caldera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final Tag.Named<Item> INERT = itemTag("inert");

    public static Tag.Named<Item> itemTag(String path) {
        return itemTag(Caldera.MODID, path);
    }

    public static Tag.Named<Item> itemTag(String modid, String path) {
        return ItemTags.createOptional(new ResourceLocation(modid, path));
    }

    public static Tag.Named<Block> blockTag(String path) {
        return blockTag(Caldera.MODID, path);
    }

    public static Tag.Named<Block> blockTag(String modid, String path) {
        return BlockTags.createOptional(new ResourceLocation(modid, path));
    }
}
