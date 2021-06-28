package caldera.common.recipe.ingredient;

import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public class FluidIngredient implements Predicate<FluidStack> {

    public static final FluidIngredient EMPTY = of(FluidStack.EMPTY);

    private final FluidStack fluidStack;
    private final ITag<Fluid> fluidTag;

    private FluidIngredient(FluidStack fluidStack, ITag<Fluid> fluidTag) {
        this.fluidStack = fluidStack;
        this.fluidTag = fluidTag;
    }

    public static FluidIngredient of(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return EMPTY;
        }
        return new FluidIngredient(fluidStack, null);
    }

    public static FluidIngredient of(ITag<Fluid> tag) {
        if (tag == null) {
            throw new NullPointerException();
        }
        return new FluidIngredient(null, tag);
    }

    public static FluidIngredient fromJson(JsonObject object, String name) {
        JsonObject ingredient = JSONUtils.getAsJsonObject(object, name);

        if (ingredient.has("tag")) {
            ResourceLocation tagName = CraftingHelper.getAsResourceLocation(ingredient, "tag");
            ITag<Fluid> fluidTag = TagCollectionManager.getInstance().getFluids().getTagOrEmpty(tagName);
            return of(fluidTag);
        } else {
            FluidStack fluidStack = CraftingHelper.readFluidStack(ingredient, true, false);
            return FluidIngredient.of(fluidStack);
        }
    }

    public JsonObject toJson() {
        if (fluidStack != null) {
            return CraftingHelper.writeFluidStack(fluidStack, true, false);
        } else {
            JsonObject object = new JsonObject();
            ResourceLocation tagName = TagCollectionManager.getInstance().getFluids().getIdOrThrow(fluidTag);
            object.addProperty("tag", tagName.toString());
            return object;
        }
    }

    public static FluidIngredient fromBuffer(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            FluidStack fluidStack = FluidStack.readFromPacket(buffer);
            return FluidIngredient.of(fluidStack);
        } else {
            ResourceLocation tagName = buffer.readResourceLocation();
            ITag<Fluid> fluidTag = TagCollectionManager.getInstance().getFluids().getTagOrEmpty(tagName);
            return of(fluidTag);
        }
    }

    public void toBuffer(PacketBuffer buffer) {
        buffer.writeBoolean(fluidStack != null);
        if (fluidStack != null) {
            fluidStack.writeToPacket(buffer);
        } else {
            ResourceLocation tagName = TagCollectionManager.getInstance().getFluids().getIdOrThrow(fluidTag);
            buffer.writeResourceLocation(tagName);
        }
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        if (this.fluidStack != null) {
            return this.fluidStack.isFluidEqual(fluidStack);
        }
        return fluidStack.getFluid().is(fluidTag);
    }
}
