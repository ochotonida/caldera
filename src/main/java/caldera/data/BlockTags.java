package caldera.data;

import caldera.Caldera;
import caldera.common.init.ModBlocks;
import caldera.common.init.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.versions.forge.ForgeVersion;

public class BlockTags extends BlockTagsProvider {

    public static final Tag.Named<Block> GLAZED_TERRACOTTA = ModTags.blockTag(ForgeVersion.MOD_ID, "glazed_terracotta");
    public static final Tag.Named<Block> CONCRETE = ModTags.blockTag(ForgeVersion.MOD_ID, "concrete");
    public static final Tag.Named<Block> CONCRETE_POWDER = ModTags.blockTag(ForgeVersion.MOD_ID, "concrete_powder");
    public static final Tag.Named<Block> SHULKER_BOXES = ModTags.blockTag(ForgeVersion.MOD_ID, "shulker_boxes");

    public BlockTags(DataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, Caldera.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.LARGE_CAULDRON.get());

        for (DyeColor color : DyeColor.values()) {
            tag(GLAZED_TERRACOTTA).add(blockById("%s_glazed_terracotta".formatted(color.getName())));
            tag(CONCRETE).add(blockById("%s_concrete".formatted(color.getName())));
            tag(CONCRETE_POWDER).add(blockById("%s_concrete_powder".formatted(color.getName())));
            tag(SHULKER_BOXES).add(blockById("%s_shulker_box".formatted(color.getName())));
        }

        tag(SHULKER_BOXES).add(Blocks.SHULKER_BOX);
    }

    private static Block blockById(String id) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
    }
}
