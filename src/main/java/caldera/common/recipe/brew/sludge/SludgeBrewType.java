package caldera.common.recipe.brew.sludge;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.brew.BrewType;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SludgeBrewType implements BrewType<SludgeBrew> {

    private final ResourceLocation id;
    private final SludgeBrew instance;

    public SludgeBrewType(ResourceLocation id, int color) {
        this.id = id;
        instance = new SludgeBrew(this, color);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity) {
        return false;
    }

    @Override
    public SludgeBrew assemble(FluidStack fluid, IItemHandler inventory, TileEntity blockEntity) {
        return instance;
    }

    @Override
    public SludgeBrew loadBrew(CompoundNBT nbt, TileEntity blockEntity) {
        return instance;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SLUDGE_BREW_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<SludgeBrewType> {

        @Override
        public SludgeBrewType fromJson(ResourceLocation id, JsonObject object) {
            int color = ColorHelper.readColor(object, "color");
            return new SludgeBrewType(id, color);
        }

        @Override
        public SludgeBrewType fromNetwork(ResourceLocation id, PacketBuffer buffer) {
            int color = buffer.readInt();
            return new SludgeBrewType(id, color);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, SludgeBrewType type) {
            buffer.writeInt(type.instance.getColor(0));
        }
    }
}
