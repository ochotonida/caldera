package caldera.block.cauldron.contents;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public abstract class CauldronContentsWithInventory implements CauldronContents {

    private final CauldronItemInventory itemInventory;

    protected CauldronContentsWithInventory(NonNullList<ItemStack> items) {
        this.itemInventory = new CauldronItemInventory();
        this.itemInventory.setItems(items);
    }

    protected CauldronContentsWithInventory(CauldronItemInventory itemInventory) {
        this.itemInventory = itemInventory;
    }

    @Override
    public CauldronItemInventory getItemHandler() {
        return itemInventory;
    }
}
