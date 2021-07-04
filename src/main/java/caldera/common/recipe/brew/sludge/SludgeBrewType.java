package caldera.common.recipe.brew.sludge;

import caldera.client.util.ColorHelper;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.Cauldron;
import caldera.common.recipe.brew.BrewType;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SludgeBrewType implements BrewType<SludgeBrew> {

    private final ResourceLocation id;
    private final int color;

    public SludgeBrewType(ResourceLocation id, int color) {
        this.id = id;
        this.color = color;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return false;
    }

    @Override
    public SludgeBrew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return new SludgeBrew(this, cauldron, color);
    }

    @Override
    public SludgeBrew loadBrew(CompoundNBT nbt, Cauldron cauldron) {
        return new SludgeBrew(this, cauldron, color);
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
            buffer.writeInt(type.color);
        }
    }
}
