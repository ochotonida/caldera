package caldera.common.recipe.conversion.item;

import caldera.common.init.ModRecipeTypes;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractItemConversionRecipe implements ItemConversionRecipe {

    protected final ResourceLocation id;
    protected final ResourceLocation conversionType;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final boolean shouldKeepNbt;

    public AbstractItemConversionRecipe(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result, boolean shouldKeepNbt) {
        this.id = id;
        this.conversionType = conversionType;
        this.ingredient = ingredient;
        this.result = result;
        this.shouldKeepNbt = shouldKeepNbt;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public ResourceLocation conversionType() {
        return conversionType;
    }

    @Override
    public boolean matches(ItemStack input) {
        return ingredient.test(input);
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ITEM_CONVERSION;
    }

    public static class Serializer<RECIPE extends AbstractItemConversionRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RECIPE> {

        private final Factory<RECIPE> factory;

        public Serializer(Factory<RECIPE> factory) {
            this.factory = factory;
        }

        @Override
        public RECIPE fromJson(ResourceLocation id, JsonObject object) {
            ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");
            Ingredient ingredient = CraftingHelper.readIngredient(object, "ingredient");
            ItemStack result = CraftingHelper.readItemStack(object, "result", true);
            if (result.getCount() != 1) {
                throw new JsonParseException("Item conversion result should have a count of 1");
            }
            boolean shouldKeepNbt = false;
            if (object.has("keepNbt")) {
                shouldKeepNbt = GsonHelper.getAsBoolean(object, "keepNbt");
            }

            return factory.create(id, conversionType, ingredient, result, shouldKeepNbt);
        }

        @Nullable
        @Override
        public RECIPE fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ResourceLocation conversionType = buffer.readResourceLocation();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            boolean shouldKeepNbt = buffer.readBoolean();

            return factory.create(id, conversionType, ingredient, result, shouldKeepNbt);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RECIPE recipe) {
            buffer.writeResourceLocation(recipe.conversionType);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeBoolean(recipe.shouldKeepNbt);
        }
    }

    public interface Factory<RECIPE extends AbstractItemConversionRecipe> {
        RECIPE create(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result, boolean shouldKeepNbt);
    }
}
