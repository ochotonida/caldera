package caldera.common.util;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class CraftingHelper {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ResourceLocation getAsResourceLocation(JsonObject object, String memberName) {
        return new ResourceLocation(JSONUtils.getAsString(object, memberName));
    }

    public static List<Ingredient> getIngredients(JsonObject object, String memberName) {
        JsonArray ingredients = JSONUtils.getAsJsonArray(object, memberName);
        List<Ingredient> result = new ArrayList<>(ingredients.size());

        for (JsonElement ingredient : ingredients) {
            result.add(net.minecraftforge.common.crafting.CraftingHelper.getIngredient(ingredient));
        }

        return result;
    }

    public static List<Ingredient> readIngredientsFromBuffer(PacketBuffer buffer) {
        int size = buffer.readByte();
        ArrayList<Ingredient> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(Ingredient.fromNetwork(buffer));
        }

        return result;
    }

    public static void writeIngredientsToBuffer(PacketBuffer buffer, List<Ingredient> ingredients) {
        buffer.writeByte(ingredients.size());

        for (Ingredient ingredient : ingredients) {
            ingredient.toNetwork(buffer);
        }
    }

    public static ItemStack readItemStack(JsonObject object, String memberName, boolean readNbt) {
        return net.minecraftforge.common.crafting.CraftingHelper
                .getItemStack(JSONUtils.getAsJsonObject(object, memberName), readNbt);
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
        return readFluidStack(JSONUtils.getAsJsonObject(object, memberName), readNbt, readAmount);
    }

    public static FluidStack readFluidStack(JsonObject object, boolean readNbt, boolean readAmount) {
        ResourceLocation fluidName = getAsResourceLocation(object, "fluid");

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);

        if (fluid == null) {
            throw new JsonSyntaxException(String.format("Unknown item '%s'", fluidName));
        } else if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }

        int amount;
        if (readAmount) {
            amount = JSONUtils.getAsInt(object, "amount");
        } else {
            amount = 1;
        }

        CompoundNBT nbt = null;
        if (readNbt && object.has("nbt")) {
            try {
                JsonElement element = object.get("nbt");
                if (element.isJsonObject()) {
                    nbt = JsonToNBT.parseTag(GSON.toJson(element));
                } else {
                    nbt = JsonToNBT.parseTag(JSONUtils.convertToString(element, "nbt"));
                }
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid NBT Entry: " + exception.toString());
            }
        }

        return new FluidStack(fluid, amount, nbt);
    }
}
