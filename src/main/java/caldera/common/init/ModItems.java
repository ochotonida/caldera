package caldera.common.init;

import caldera.Caldera;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("unused")
public class ModItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, Caldera.MODID);

    public static final ItemGroup CREATIVE_TAB = new ItemGroup(Caldera.MODID) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(LARGE_CAULDRON.get());
        }
    };

    public static final RegistryObject<Item> LARGE_CAULDRON = REGISTRY.register("large_cauldron", () -> new BlockItem(ModBlocks.LARGE_CAULDRON.get(), new Item.Properties().tab(CREATIVE_TAB)));

}
