package caldera.block.cauldron.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class CauldronItemInventory extends ItemStackHandler {

    public static final int DEFAULT_INVENTORY_SIZE = 8;
    public static final int DEFAULT_SLOT_SIZE = 1;

    public static final Codec<CauldronItemInventory> CODEC = codec(DEFAULT_INVENTORY_SIZE, DEFAULT_SLOT_SIZE);

    private final int slotSize;

    public CauldronItemInventory() {
        this(DEFAULT_INVENTORY_SIZE, DEFAULT_SLOT_SIZE);
    }

    public CauldronItemInventory(int size, int slotSize) {
        super(size);
        this.slotSize = slotSize;
    }

    public static CauldronItemInventory withItems(int size, int slotSize, List<ItemStack> items) {
        CauldronItemInventory itemInventory = new CauldronItemInventory(size, slotSize);
        itemInventory.setItems(items);
        return itemInventory;
    }

    public static Codec<CauldronItemInventory> codec(int size, int slotSize) {
        return ItemStack.OPTIONAL_CODEC
                .validate(stack -> {
                    if (stack.getCount() > size) {
                        return DataResult.error(() -> "Item stack [%s] is larger slot size of %s".formatted(stack, slotSize));
                    }
                    return DataResult.success(stack);
                }).listOf(size, size)
                .xmap(
                        items -> withItems(size, slotSize, items),
                        inventory -> inventory.stacks
                );
    }

    public void setItems(List<ItemStack> items) {
        if (items.size() != getSlots()) {
            throw new IllegalArgumentException();
        }
        for (int slot = 0; slot < items.size(); slot++) {
            setStackInSlot(slot, items.get(slot));
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotSize;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (stack.getCount() > getSlotLimit(slot)) {
            throw new IllegalArgumentException("Item stack [%s] exceeds slot capacity of %s".formatted(stack, getSlotLimit(slot)));
        }
        super.setStackInSlot(slot, stack);
    }
}
