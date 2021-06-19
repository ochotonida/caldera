package caldera.common.block.cauldron;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.IItemHandler;

import java.util.Stack;

public class CauldronItemHandler implements IItemHandler {

    private final Stack<ItemStack> items;
    private final CauldronBlockEntity cauldron;

    public CauldronItemHandler(CauldronBlockEntity cauldron) {
        items = new Stack<>();
        this.cauldron = cauldron;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addItem(ItemStack stack) {
        items.push(stack.split(1));
        onContentsChanged();
    }

    public void clear() {
        items.clear();
        onContentsChanged();
    }

    protected void onContentsChanged() {
        cauldron.setChanged();
    }

    public void readFromNBT(INBT nbt) {
        clear();

        if (nbt instanceof ListNBT) {
            for (INBT itemNBT : ((ListNBT) nbt)) {
                if (itemNBT instanceof CompoundNBT) {
                    items.push(ItemStack.of(((CompoundNBT) itemNBT)));
                }
            }
        }
    }

    public ListNBT writeToNBT() {
        ListNBT listNBT = new ListNBT();
        for (ItemStack item : items) {
            listNBT.add(item.save(new CompoundNBT()));
        }
        return listNBT;
    }

    @Override
    public int getSlots() {
        return items.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return items.get(slot);
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
