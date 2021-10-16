package caldera.data;

import caldera.Caldera;
import caldera.common.init.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.versions.forge.ForgeVersion;

@SuppressWarnings("unchecked")
public class ItemTags extends ItemTagsProvider {

    public static final Tag.Named<Item> TOOLS = ModTags.itemTag(ForgeVersion.MOD_ID, "tools");
    public static final Tag.Named<Item> AXES = ModTags.itemTag(ForgeVersion.MOD_ID, "tools/axes");
    public static final Tag.Named<Item> HOES = ModTags.itemTag(ForgeVersion.MOD_ID, "tools/hoes");
    public static final Tag.Named<Item> PICKAXES = ModTags.itemTag(ForgeVersion.MOD_ID, "tools/pickaxes");
    public static final Tag.Named<Item> SHOVELS = ModTags.itemTag(ForgeVersion.MOD_ID, "tools/shovels");
    public static final Tag.Named<Item> SWORDS = ModTags.itemTag(ForgeVersion.MOD_ID, "tools/swords");


    public ItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagProvider, Caldera.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        addToolTags();

        tag(ModTags.INERT).addTags(TOOLS);
    }

    private void addToolTags() {
        tag(TOOLS).addTags(AXES, HOES, PICKAXES, SHOVELS, SWORDS).add(
                Items.BOW, Items.CARROT_ON_A_STICK, Items.CROSSBOW, Items.FISHING_ROD, Items.FLINT_AND_STEEL,
                Items.SHIELD, Items.SHEARS, Items.TOTEM_OF_UNDYING, Items.TRIDENT, Items.WARPED_FUNGUS_ON_A_STICK
        );

        tag(AXES).add(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE,
                Items.NETHERITE_AXE
        );
        tag(HOES).add(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE,
                Items.NETHERITE_HOE
        );
        tag(PICKAXES).add(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE,
                Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE
        );
        tag(SHOVELS).add(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL,
                Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL
        );
        tag(SWORDS).add(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD,
                Items.DIAMOND_SWORD, Items.NETHERITE_SWORD
        );
    }
}
