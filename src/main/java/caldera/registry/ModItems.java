package caldera.registry;

import caldera.Caldera;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Caldera.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Caldera.MOD_ID);

    @SuppressWarnings("unused")
    public static final Holder<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("%s.creative_tab".formatted(Caldera.MOD_ID)))
            .icon(() -> new ItemStack(ModItems.LARGE_CAULDRON))
            .displayItems((itemDisplayParameters, output) -> {
                for (DeferredHolder<Item, ? extends Item> entry : ITEMS.getEntries()) {
                    output.accept(entry.get());
                }
            })
            .build());

    public static final Holder<Item> LARGE_CAULDRON = ITEMS.register("large_cauldron", () -> new BlockItem(ModBlocks.LARGE_CAULDRON.get(), new Item.Properties()));

}
