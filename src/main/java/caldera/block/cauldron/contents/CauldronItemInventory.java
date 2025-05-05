package caldera.block.cauldron.contents;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class CauldronItemInventory extends ItemStackHandler {

    public static final int INVENTORY_SIZE = 8;
    public static final int SLOT_LIMIT = 1;

    public CauldronItemInventory() {
        super(INVENTORY_SIZE);
    }

    public void setItems(List<ItemStack> items) {
        for (int slot = 0; slot < items.size(); slot++) {
            setStackInSlot(slot, items.get(slot));
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return SLOT_LIMIT;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (stack.getCount() > getSlotLimit(slot)) {
            throw new IllegalArgumentException("Item stack [%s] exceeds slot capacity of %s".formatted(stack, getSlotLimit(slot)));
        }
        super.setStackInSlot(slot, stack);
    }
}
