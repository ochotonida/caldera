package caldera.block.cauldron.contents;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class CauldronContentsWithInventory implements CauldronContents {

    private final CauldronItemInventory itemInventory;

    protected CauldronContentsWithInventory(NonNullList<ItemStack> items) {
        this.itemInventory = new CauldronItemInventory();
        this.itemInventory.setItems(items);
    }

    @Override
    public IItemHandler getItemHandler() {
        return itemInventory;
    }
}
