package caldera.common.block.cauldron;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CauldronItemHandler extends ItemStackHandler {

    public static final int CAPACITY = 8;

    private final CauldronBlockEntity cauldron;

    public CauldronItemHandler(CauldronBlockEntity cauldron) {
        super(CAPACITY);
        this.cauldron = cauldron;
    }

    public boolean isEmpty() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFull() {
        return getNextEmptySlot() == getSlots();
    }

    private int getNextEmptySlot() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return getSlots();
    }

    public void addItem(ItemStack stack) {
        if (!isFull() && !stack.isEmpty()) {
            stacks.set(getNextEmptySlot(), stack.split(1));
            onContentsChanged();
        }
    }

    public void clear() {
        stacks.clear();
        onContentsChanged();
    }

    @Override
    protected void onContentsChanged(int slot) {
        onContentsChanged();
    }

    protected void onContentsChanged() {
        cauldron.setChanged(); // TODO perhaps it's better to do this in the cauldronBlockEntity itself
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }
}
