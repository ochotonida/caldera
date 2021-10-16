package caldera.common.recipe.brew.sludge;

import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.Cauldron;
import caldera.common.recipe.brew.BrewType;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
    public SludgeBrew assemble(FluidStack fluid, IItemHandler inventory, Cauldron cauldron) {
        return new SludgeBrew(this, cauldron, color);
    }

    @Override
    public SludgeBrew loadBrew(CompoundTag nbt, Cauldron cauldron) {
        return new SludgeBrew(this, cauldron, color);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SLUDGE_BREW_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<SludgeBrewType> {

        @Override
        public SludgeBrewType fromJson(ResourceLocation id, JsonObject object) {
            int color = ColorHelper.readColor(object, "color");
            return new SludgeBrewType(id, color);
        }

        @Override
        public SludgeBrewType fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int color = buffer.readInt();
            return new SludgeBrewType(id, color);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SludgeBrewType type) {
            buffer.writeInt(type.color);
        }
    }
}
