package caldera.data.recipe;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SheepDyeingRecipeBuilder {

    private ResourceLocation conversionType;
    @Nullable
    private final DyeColor input;
    private final DyeColor result;

    public SheepDyeingRecipeBuilder(@Nullable DyeColor input, DyeColor result) {
        this.input = input;
        this.result = result;
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        for (DyeColor color : DyeColor.values()) {
            save(consumer, new ResourceLocation(Caldera.MODID, "dye_%s".formatted(color.getName())), convert(color));
        }
    }

    public static SheepDyeingRecipeBuilder convert(DyeColor result) {
        return convert(null, result);
    }

    public static SheepDyeingRecipeBuilder convert(@Nullable DyeColor input, DyeColor result) {
        return new SheepDyeingRecipeBuilder(input, result);
    }

    public SheepDyeingRecipeBuilder setConversionType(ResourceLocation conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    public static void save(Consumer<FinishedRecipe> consumer, ResourceLocation transmutationType, SheepDyeingRecipeBuilder... builders) {
        for (SheepDyeingRecipeBuilder builder : builders) {
            builder.setConversionType(transmutationType).save(consumer);
        }
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (conversionType == null) {
            throw new IllegalStateException();
        }
        consumer.accept(new SheepDyeingRecipeBuilder.Result(id, conversionType, input, result));
    }

    public void save(Consumer<FinishedRecipe> consumer, String name) {
        save(consumer, new ResourceLocation(Caldera.MODID, name));
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        String path = "conversion/entity/%s/dye_sheep_%s".formatted(conversionType.getPath(), result);
        save(consumer, path);
    }

    public record Result(ResourceLocation id, ResourceLocation conversionType, @Nullable DyeColor input, DyeColor result) implements FinishedRecipe {

        public void serializeRecipeData(JsonObject object) {
            object.addProperty("conversionType", conversionType.toString());
            if (input != null) {
                object.add("input", JsonHelper.writeEnumValue(input));
            }
            object.add("result", JsonHelper.writeEnumValue(result));
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public RecipeSerializer<?> getType() {
            return ModRecipeTypes.SHEEP_DYEING_SERIALIZER.get();
        }

        @Nullable
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
