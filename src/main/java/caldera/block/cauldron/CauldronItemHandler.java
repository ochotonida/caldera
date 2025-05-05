package caldera.block.cauldron;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class CauldronItemHandler implements IItemHandler {

    private final Cauldron cauldron;

    public CauldronItemHandler(Cauldron cauldron) {
        this.cauldron = cauldron;
    }

    @Override
    public int getSlots() {
        return cauldron.getContents().getItemHandler().getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return cauldron.getContents().getItemHandler().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return cauldron.getContents().getItemHandler().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int count, boolean simulate) {
        return cauldron.getContents().getItemHandler().extractItem(slot, count, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return cauldron.getContents().getItemHandler().getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return cauldron.getContents().getItemHandler().isItemValid(slot, stack);
    }
}
