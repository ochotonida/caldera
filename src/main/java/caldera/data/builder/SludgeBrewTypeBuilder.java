package caldera.data.builder;

import caldera.Caldera;
import caldera.client.util.ColorHelper;
import caldera.common.init.ModRecipeTypes;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class SludgeBrewTypeBuilder {

    private final int color;

    public SludgeBrewTypeBuilder(int color) {
        this.color = color;
    }

    public static void addRecipes(Consumer<IFinishedRecipe> consumer) {
        sludgeBrewType(0x873954).build(consumer, "sludge");
    }

    public static SludgeBrewTypeBuilder sludgeBrewType(int color) {
        return new SludgeBrewTypeBuilder(color);
    }

    public void build(Consumer<IFinishedRecipe> consumer, String location) {
        build(consumer, new ResourceLocation(Caldera.MODID, "brew_types/" + location));
    }

    public void build(Consumer<IFinishedRecipe> consumer, ResourceLocation id) {
        consumer.accept(new SludgeBrewTypeBuilder.Result(id, color));
    }

    public static class Result implements IFinishedRecipe {

        private final ResourceLocation id;
        private final int color;

        public Result(ResourceLocation id, int color) {
            this.id = id;
            this.color = color;
        }

        @Override
        public void serializeRecipeData(JsonObject object) {
            object.add("color", ColorHelper.writeColor(color));
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public IRecipeSerializer<?> getType() {
            return ModRecipeTypes.SLUDGE_BREW_SERIALIZER.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
