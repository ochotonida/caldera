package caldera.data.recipe;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemConversionRecipeBuilder {

    private ResourceLocation conversionType;
    private boolean isToolRecipe;
    private final ItemStack result;
    private final Ingredient ingredient;

    public ItemConversionRecipeBuilder(Item result, Ingredient ingredient) {
        this.result = new ItemStack(result);
        this.ingredient = ingredient;
    }

    @SuppressWarnings("unchecked")
    public static void addRecipes(Consumer<FinishedRecipe> consumer) {
        save(consumer, new ResourceLocation(Caldera.MODID, "iron_to_gold"),
                convert(Blocks.GOLD_ORE, Blocks.IRON_ORE),
                convert(Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE),
                convert(Blocks.GILDED_BLACKSTONE, Blocks.BLACKSTONE),
                convert(Blocks.RAW_GOLD_BLOCK, Blocks.RAW_IRON_BLOCK),
                convert(Blocks.GOLD_BLOCK, Tags.Items.STORAGE_BLOCKS_IRON),
                convert(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE),
                convert(Blocks.POWERED_RAIL, Blocks.ACTIVATOR_RAIL),
                convert(Items.GOLD_INGOT, Tags.Items.INGOTS_IRON),
                convert(Items.GOLD_NUGGET, Tags.Items.NUGGETS_IRON),
                convert(Items.RAW_GOLD, Items.RAW_IRON),
                convert(Items.GOLDEN_APPLE, Items.APPLE),
                convert(Items.GOLDEN_CARROT, Items.CARROT),
                convert(Items.GOLDEN_HORSE_ARMOR, Items.IRON_HORSE_ARMOR),
                convert(Items.CLOCK, Items.COMPASS),
                convert(Items.GLISTERING_MELON_SLICE, Items.MELON_SLICE),
                convert(Items.MUSIC_DISC_PIGSTEP, Items.MUSIC_DISC_11),
                convert(Items.GOLDEN_AXE, Items.IRON_AXE).setToolRecipe(),
                convert(Items.GOLDEN_HOE, Items.IRON_HOE).setToolRecipe(),
                convert(Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE).setToolRecipe(),
                convert(Items.GOLDEN_SHOVEL, Items.IRON_SHOVEL).setToolRecipe(),
                convert(Items.GOLDEN_SWORD, Items.IRON_SWORD).setToolRecipe(),
                convert(Items.GOLDEN_BOOTS, Items.IRON_BOOTS).setToolRecipe(),
                convert(Items.GOLDEN_LEGGINGS, Items.IRON_LEGGINGS).setToolRecipe(),
                convert(Items.GOLDEN_CHESTPLATE, Items.IRON_CHESTPLATE).setToolRecipe(),
                convert(Items.GOLDEN_HELMET, Items.IRON_HELMET).setToolRecipe()
        );
    }

    public static void save(Consumer<FinishedRecipe> consumer, ResourceLocation transmutationType, ItemConversionRecipeBuilder... builders) {
        for (ItemConversionRecipeBuilder builder : builders) {
            builder.setConversionType(transmutationType).save(consumer);
        }
    }

    public static ItemConversionRecipeBuilder convert(ItemLike result, ItemLike... ingredients) {
        return convert(result.asItem(), Ingredient.of(ingredients));
    }

    @SuppressWarnings("unchecked")
    public static ItemConversionRecipeBuilder convert(ItemLike result, Tag<Item>... ingredients) {
        return convert(result.asItem(), Ingredient.merge(Arrays.stream(ingredients).map(Ingredient::of).collect(Collectors.toList())));
    }

    public static ItemConversionRecipeBuilder convert(Item result, Ingredient ingredient) {
        return new ItemConversionRecipeBuilder(result, ingredient);
    }

    public ItemConversionRecipeBuilder setConversionType(ResourceLocation conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    public ItemConversionRecipeBuilder setToolRecipe() {
        this.isToolRecipe = true;
        return this;
    }

    public ItemStack getResult() {
        return result;
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (conversionType == null) {
            throw new IllegalStateException();
        }
        consumer.accept(new Result(id, conversionType, ingredient, result, isToolRecipe));
    }

    public void save(Consumer<FinishedRecipe> consumer, String name) {
        save(consumer, new ResourceLocation(Caldera.MODID, name));
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        // noinspection ConstantConditions
        String path = "conversion/item/%s/%s".formatted(conversionType.getPath(), getResult().getItem().getRegistryName().getPath());
        save(consumer, path);
    }

    public record Result(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result, boolean isToolConversion) implements FinishedRecipe {

        public void serializeRecipeData(JsonObject object) {
            object.addProperty("conversionType", conversionType.toString());
            object.add("ingredient", this.ingredient.toJson());
            if (isToolConversion) {
                object.addProperty("copyEnchantments", true);
            }
            object.add("result", CraftingHelper.writeItemStack(result));
        }

        public ResourceLocation getId() {
            return this.id;
        }

        public RecipeSerializer<?> getType() {
            return isToolConversion
                    ? ModRecipeTypes.TOOL_CONVERSION_SERIALIZER.get()
                    : ModRecipeTypes.ITEM_CONVERSION_SERIALIZER.get();
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
