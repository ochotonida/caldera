package caldera.common.recipe.ingredient;

import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public record FluidIngredient(FluidStack fluidStack, Tag<Fluid> fluidTag) implements Predicate<FluidStack> {

    public static final FluidIngredient EMPTY = of(FluidStack.EMPTY);

    public static FluidIngredient of(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return EMPTY;
        }
        return new FluidIngredient(fluidStack, null);
    }

    public static FluidIngredient of(Tag<Fluid> tag) {
        if (tag == null) {
            throw new NullPointerException();
        }
        return new FluidIngredient(null, tag);
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        if (this.fluidStack != null) {
            return this.fluidStack.isFluidEqual(fluidStack);
        }
        return fluidStack.getFluid().is(fluidTag);
    }

    public static FluidIngredient fromJson(JsonObject object, String name) {
        JsonObject ingredient = GsonHelper.getAsJsonObject(object, name);

        if (ingredient.has("tag")) {
            ResourceLocation tagName = CraftingHelper.readResourceLocation(ingredient, "tag");
            Tag<Fluid> fluidTag = SerializationTags.getInstance().getTagOrThrow(Registry.FLUID_REGISTRY, tagName, (id) -> new JsonSyntaxException("Unknown fluid tag '" + id + "'"));
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
            ResourceLocation tagName = SerializationTags.getInstance().getIdOrThrow(Registry.FLUID_REGISTRY, fluidTag, () -> new IllegalStateException("Unknown fluid tag"));
            object.addProperty("tag", tagName.toString());
            return object;
        }
    }

    public static FluidIngredient fromBuffer(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            FluidStack fluidStack = FluidStack.readFromPacket(buffer);
            return FluidIngredient.of(fluidStack);
        } else {
            ResourceLocation tagName = buffer.readResourceLocation();
            Tag<Fluid> fluidTag = SerializationTags.getInstance().getTagOrThrow(Registry.FLUID_REGISTRY, tagName, (id) -> null);
            return of(fluidTag);
        }
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(fluidStack != null);
        if (fluidStack != null) {
            fluidStack.writeToPacket(buffer);
        } else {
            ResourceLocation tagName = SerializationTags.getInstance().getIdOrThrow(Registry.FLUID_REGISTRY, fluidTag, () -> null);
            buffer.writeResourceLocation(tagName);
        }
    }
}
