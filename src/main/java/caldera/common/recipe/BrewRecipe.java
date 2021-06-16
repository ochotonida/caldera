package caldera.common.recipe;

import caldera.common.init.ModRecipeTypes;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public abstract class BrewRecipe<T extends Brew> implements IRecipe<IInventory> {

    private final ResourceLocation id;

    public BrewRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public final ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeType<?> getType() {
        return ModRecipeTypes.BREW;
    }

    abstract boolean matches(FluidStack fluid, IInventory inventory, World level, BlockPos pos);

    abstract T createBrew(FluidStack fluid, IInventory inventory, World level, BlockPos pos);

    abstract T loadBrew(CompoundNBT nbt, World level, BlockPos pos);

    public T loadBrew(PacketBuffer buffer, World level, BlockPos pos) {
        return loadBrew(buffer.readNbt(), level, pos);
    }

    // unused
    @Override
    public final boolean matches(IInventory inventory, World level) {
        return false;
    }

    // unused
    @Override
    public final ItemStack assemble(IInventory inventory) {
        return null;
    }

    // unused
    @Override
    public final boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // unused
    @Override
    public final ItemStack getResultItem() {
        return null;
    }
}
