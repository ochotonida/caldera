package caldera.data.recipe;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.ingredient.EntityIngredient;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EntityConversionRecipeBuilder {

    private ResourceLocation conversionType;
    private final EntityType<?> result;
    private final EntityIngredient ingredient;

    public EntityConversionRecipeBuilder(EntityType<?> result, EntityIngredient ingredient) {
        this.result = result;
        this.ingredient = ingredient;
    }

    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        save(consumer, new ResourceLocation(Caldera.MODID, "test_conversion"),
                convert(EntityType.ZOMBIE_VILLAGER, EntityType.VILLAGER),
                convert(EntityType.WITCH, EntityType.ZOMBIE_VILLAGER)
        );
    }

    public static void save(Consumer<FinishedRecipe> consumer, ResourceLocation transmutationType, EntityConversionRecipeBuilder... builders) {
        for (EntityConversionRecipeBuilder builder : builders) {
            builder.setConversionType(transmutationType).save(consumer);
        }
    }

    public static EntityConversionRecipeBuilder convert(EntityType<?> result, Tag<EntityType<?>> ingredient) {
        return convert(result, EntityIngredient.of(ingredient));
    }

    public static EntityConversionRecipeBuilder convert(EntityType<?> result, EntityType<?> ingredient) {
        return convert(result, EntityIngredient.of(ingredient));
    }

    public static EntityConversionRecipeBuilder convert(EntityType<?> result, EntityIngredient ingredient) {
        return new EntityConversionRecipeBuilder(result, ingredient);
    }

    public EntityConversionRecipeBuilder setConversionType(ResourceLocation conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    public EntityType<?> getResult() {
        return result;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (conversionType == null) {
            throw new IllegalStateException();
        }
        consumer.accept(new EntityConversionRecipeBuilder.Result(id, conversionType, ingredient, result));
    }

    public void save(Consumer<FinishedRecipe> consumer, String name) {
        save(consumer, new ResourceLocation(Caldera.MODID, name));
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        // noinspection ConstantConditions
        String path = "conversion/entity/%s/%s".formatted(conversionType.getPath(), getResult().getRegistryName().getPath());
        save(consumer, path);
    }

    public record Result(ResourceLocation id, ResourceLocation conversionType, EntityIngredient ingredient, EntityType<?> result) implements FinishedRecipe {

        public void serializeRecipeData(JsonObject object) {
            object.addProperty("conversionType", conversionType.toString());
            object.add("ingredient", this.ingredient.toJson());
            // noinspection ConstantConditions
            object.addProperty("result", result.getRegistryName().toString());
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public RecipeSerializer<?> getType() {
            return ModRecipeTypes.ENTITY_CONVERSION_SERIALIZER.get();
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
