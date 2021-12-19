package caldera.common.recipe.conversion.entity;

import caldera.common.init.ModRecipeTypes;
import caldera.common.util.CraftingHelper;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public record SheepDyeingRecipe(ResourceLocation id, ResourceLocation conversionType, DyeColor result, @Nullable DyeColor input) implements EntityConversionRecipe {

    @Override
    public boolean matches(LivingEntity entity) {
        return entity instanceof Sheep sheep && (input == null || sheep.getColor() == result);
    }

    @Nullable
    @Override
    public LivingEntity convertEntity(ServerLevel level, LivingEntity input) {
        if (input instanceof Sheep sheep) {
            sheep.setColor(result);
            return input;
        }
        return null;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.SHEEP_DYEING_SERIALIZER.get();
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<SheepDyeingRecipe> {

        @Override
        public SheepDyeingRecipe fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            DyeColor result = JsonHelper.getAsEnumValue(object, "result", DyeColor.class);
            DyeColor input = null;
            if (object.has("input")) {
                input = JsonHelper.getAsEnumValue(object, "input", DyeColor.class);
            }
            return new SheepDyeingRecipe(id, conversionType, result, input);
        }

        @Nullable
        @Override
        public SheepDyeingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            DyeColor result = buffer.readEnum(DyeColor.class);
            DyeColor input = null;
            if (buffer.readBoolean()) {
                input = buffer.readEnum(DyeColor.class);
            }
            return new SheepDyeingRecipe(id, conversionType, result, input);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SheepDyeingRecipe recipe) {
            buffer.writeResourceLocation(recipe.conversionType);
            buffer.writeEnum(recipe.result);
            buffer.writeBoolean(recipe.input != null);
            if (recipe.input != null) {
                buffer.writeEnum(recipe.input);
            }
        }
    }
}
