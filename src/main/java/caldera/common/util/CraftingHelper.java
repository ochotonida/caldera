package caldera.common.util;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class CraftingHelper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ResourceLocation readResourceLocation(JsonObject object, String memberName) {
        return new ResourceLocation(GsonHelper.getAsString(object, memberName));
    }

    public static JsonArray writeIngredients(List<Ingredient> ingredients) {
        JsonArray array = new JsonArray();

        for (Ingredient ingredient : ingredients) {
            array.add(ingredient.toJson());
        }

        return array;
    }

    public static void writeIngredients(FriendlyByteBuf buffer, List<Ingredient> ingredients) {
        buffer.writeByte(ingredients.size());

        for (Ingredient ingredient : ingredients) {
            ingredient.toNetwork(buffer);
        }
    }

    public static List<Ingredient> readIngredients(JsonObject object, String memberName) {
        JsonArray ingredients = GsonHelper.getAsJsonArray(object, memberName);
        List<Ingredient> result = new ArrayList<>(ingredients.size());

        for (JsonElement ingredient : ingredients) {
            result.add(net.minecraftforge.common.crafting.CraftingHelper.getIngredient(ingredient));
        }

        return result;
    }

    public static List<Ingredient> readIngredients(FriendlyByteBuf buffer) {
        int size = buffer.readByte();
        ArrayList<Ingredient> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(Ingredient.fromNetwork(buffer));
        }

        return result;
    }

    public static JsonObject writeItemStack(ItemStack stack) {
        JsonObject object = new JsonObject();

        // noinspection ConstantConditions
        object.addProperty("item", stack.getItem().getRegistryName().toString());
        object.addProperty("count", stack.getCount());
        if (stack.hasTag()) {
            // noinspection ConstantConditions
            object.addProperty("nbt", stack.getTag().toString());
        }

        return object;
    }

    public static ItemStack readItemStack(JsonObject object, String memberName, boolean readNbt) {
        return net.minecraftforge.common.crafting.CraftingHelper
                .getItemStack(GsonHelper.getAsJsonObject(object, memberName), readNbt);
    }

    public static JsonObject writeFluidStack(FluidStack fluidStack, boolean writeNbt, boolean writeAmount) {
        JsonObject object = new JsonObject();

        if (fluidStack.isEmpty()) {
            object.addProperty("fluid", "minecraft:empty");
        } else {
            // noinspection ConstantConditions
            object.addProperty("fluid", fluidStack.getFluid().getRegistryName().toString());
            if (fluidStack.hasTag() && writeNbt) {
                object.addProperty("nbt", fluidStack.getTag().toString());
            }
            if (writeAmount) {
                object.addProperty("amount", fluidStack.getAmount());
            }
        }

        return object;
    }

    public static FluidStack readFluidStack(JsonObject object, String memberName, boolean readNbt, boolean readAmount) {
        return readFluidStack(GsonHelper.getAsJsonObject(object, memberName), readNbt, readAmount);
    }

    public static FluidStack readFluidStack(JsonObject object, boolean readNbt, boolean readAmount) {
        ResourceLocation fluidName = readResourceLocation(object, "fluid");

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);

        if (fluid == null) {
            throw new JsonSyntaxException("Unknown item '%s'".formatted(fluidName));
        } else if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }

        int amount;
        if (readAmount) {
            amount = GsonHelper.getAsInt(object, "amount");
        } else {
            amount = 1;
        }

        CompoundTag nbt = null;
        if (readNbt && object.has("nbt")) {
            try {
                JsonElement element = object.get("nbt");
                if (element.isJsonObject()) {
                    nbt = TagParser.parseTag(GSON.toJson(element));
                } else {
                    nbt = TagParser.parseTag(GsonHelper.convertToString(element, "nbt"));
                }
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid NBT Entry: " + exception);
            }
        }

        return new FluidStack(fluid, amount, nbt);
    }
}
