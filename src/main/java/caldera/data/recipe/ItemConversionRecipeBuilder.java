package caldera.data.recipe;

import caldera.Caldera;
import caldera.common.init.ModRecipeTypes;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemConversionRecipeBuilder {

    private ResourceLocation conversionType;
    private boolean isToolRecipe;
    private final ItemStack result;
    private final Ingredient ingredient;
    private boolean shouldKeepNbt;

    public ItemConversionRecipeBuilder(ItemStack result, Ingredient ingredient, boolean shouldKeepNbt) {
        this.result = result;
        this.ingredient = ingredient;
        this.shouldKeepNbt = shouldKeepNbt;
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

        for (DyeColor color : DyeColor.values()) {
            ResourceLocation conversionType = new ResourceLocation(Caldera.MODID, "dyeing/%s".formatted(color.getName()));
            save(consumer, conversionType,
                    convert(itemById("%s_wool".formatted(color.getName())), ItemTags.WOOL),
                    convert(itemById("%s_carpet".formatted(color.getName())), ItemTags.CARPETS),
                    convert(itemById("%s_stained_glass".formatted(color.getName())), Tags.Items.STAINED_GLASS),
                    convert(itemById("%s_stained_glass_pane".formatted(color.getName())), Tags.Items.STAINED_GLASS_PANES),
                    convert(itemById("%s_terracotta".formatted(color.getName())), ItemTags.TERRACOTTA),
                    convert(itemById("%s_glazed_terracotta".formatted(color.getName())), caldera.data.ItemTags.GLAZED_TERRACOTTA),
                    convert(itemById("%s_concrete".formatted(color.getName())), caldera.data.ItemTags.CONCRETE),
                    convert(itemById("%s_concrete_powder".formatted(color.getName())), caldera.data.ItemTags.CONCRETE_POWDER),
                    convert(itemById("%s_bed".formatted(color.getName())), ItemTags.BEDS),
                    convert(itemById("%s_candle".formatted(color.getName())), ItemTags.CANDLES),
                    convert(itemById("%s_dye".formatted(color.getName())), Tags.Items.DYES),
                    convert(itemById("%s_shulker_box".formatted(color.getName())), caldera.data.ItemTags.SHULKER_BOXES).setKeepNbt()
            );

            convert(itemById("%s_stained_glass".formatted(color.getName())), Items.GLASS)
                    .setConversionType(conversionType)
                    .save(consumer, "dyeing/%s/%s_stained_glass_from_glass".formatted(color.getName(), color.getName()));
            convert(itemById("%s_stained_glass_pane".formatted(color.getName())), Items.GLASS_PANE)
                    .setConversionType(conversionType)
                    .save(consumer, "dyeing/%s/%s_stained_glass_pane_from_glass_pane".formatted(color.getName(), color.getName()));
        }

        convert(itemById("red_tulip"), Ingredient.of(Items.ORANGE_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP)).setConversionType("dyeing/red").save(consumer, "dyeing/red/red_tulip");
        convert(itemById("orange_tulip"), Ingredient.of(Items.RED_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP)).setConversionType("dyeing/orange").save(consumer, "dyeing/orange/orange_tulip");
        convert(itemById("white_tulip"), Ingredient.of(Items.RED_TULIP, Items.ORANGE_TULIP, Items.PINK_TULIP)).setConversionType("dyeing/white").save(consumer, "dyeing/white/white_tulip");
        convert(itemById("pink_tulip"), Ingredient.of(Items.RED_TULIP, Items.ORANGE_TULIP, Items.WHITE_TULIP)).setConversionType("dyeing/pink").save(consumer, "dyeing/pink/pink_tulip");
    }

    private static Item itemById(String id) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
    }

    public static ItemConversionRecipeBuilder convert(ItemLike result, ItemLike... ingredients) {
        return convert(result.asItem(), Ingredient.of(ingredients));
    }

    @SuppressWarnings("unchecked")
    public static ItemConversionRecipeBuilder convert(ItemLike result, Tag<Item>... ingredients) {
        return convert(result.asItem(), Ingredient.merge(Arrays.stream(ingredients).map(Ingredient::of).collect(Collectors.toList())));
    }

    public static ItemConversionRecipeBuilder convert(Item result, Ingredient ingredient) {
        return convert(new ItemStack(result), ingredient);
    }

    public static ItemConversionRecipeBuilder convert(ItemStack result, Ingredient ingredient) {
        if (result.getCount() != 1) {
            throw new IllegalArgumentException("Stack size must be 1");
        }
        return new ItemConversionRecipeBuilder(result, ingredient, false);
    }

    public ItemConversionRecipeBuilder setConversionType(String conversionType) {
        return setConversionType(new ResourceLocation(Caldera.MODID, conversionType));
    }

    public ItemConversionRecipeBuilder setConversionType(ResourceLocation conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    public ItemConversionRecipeBuilder setToolRecipe() {
        this.isToolRecipe = true;
        return setKeepNbt();
    }

    public ItemConversionRecipeBuilder setKeepNbt() {
        this.shouldKeepNbt = true;
        return this;
    }

    public static void save(Consumer<FinishedRecipe> consumer, ResourceLocation transmutationType, ItemConversionRecipeBuilder... builders) {
        for (ItemConversionRecipeBuilder builder : builders) {
            builder.setConversionType(transmutationType).save(consumer);
        }
    }

    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (conversionType == null) {
            throw new IllegalStateException();
        }
        id = new ResourceLocation(id.getNamespace(), "conversion/item/" + id.getPath());
        consumer.accept(new Result(id, conversionType, ingredient, result, isToolRecipe, shouldKeepNbt));
    }

    public void save(Consumer<FinishedRecipe> consumer, String name) {
        save(consumer, new ResourceLocation(Caldera.MODID, name));
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        // noinspection ConstantConditions
        String path = "%s/%s".formatted(conversionType.getPath(), result.getItem().getRegistryName().getPath());
        save(consumer, path);
    }

    public record Result(ResourceLocation id, ResourceLocation conversionType, Ingredient ingredient, ItemStack result, boolean isToolConversion, boolean shouldKeepNbt) implements FinishedRecipe {

        public void serializeRecipeData(JsonObject object) {
            object.addProperty("conversionType", conversionType.toString());
            object.add("ingredient", this.ingredient.toJson());
            if (shouldKeepNbt) {
                object.addProperty("keepNbt", true);
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
