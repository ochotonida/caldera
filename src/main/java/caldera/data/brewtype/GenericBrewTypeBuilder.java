package caldera.data.brewtype;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.init.CalderaRegistries;
import caldera.common.init.ModBrewTypes;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class GenericBrewTypeBuilder {

    private final int color;

    public GenericBrewTypeBuilder(int color) {
        this.color = color;
    }

    public static void addRecipes(Consumer<FinishedBrewType> consumer) {
        sludgeBrewType(0x873954).build(consumer, "sludge");
    }

    public static GenericBrewTypeBuilder sludgeBrewType(int color) {
        return new GenericBrewTypeBuilder(color);
    }

    public void build(Consumer<FinishedBrewType> consumer, String location) {
        build(consumer, new ResourceLocation(Caldera.MODID, "brew_types/" + location));
    }

    public void build(Consumer<FinishedBrewType> consumer, ResourceLocation id) {
        consumer.accept(new GenericBrewTypeBuilder.Result(id, color));
    }

    public static class Result implements FinishedBrewType {

        private final ResourceLocation id;
        private final int color;

        public Result(ResourceLocation id, int color) {
            this.id = id;
            this.color = color;
        }

        public void serializeBrewTypeData(JsonObject object) {
            object.add("color", ColorHelper.writeColor(color));
        }

        public ResourceLocation getId() {
            return id;
        }

        public BrewTypeSerializer<?> getType() {
            return ModBrewTypes.GENERIC_BREW_SERIALIZER.get();
        }

        public JsonObject serializeBrewType() {
            JsonObject jsonobject = new JsonObject();
            // noinspection ConstantConditions
            jsonobject.addProperty("type", CalderaRegistries.BREW_TYPE_SERIALIZERS.getKey(getType()).toString());
            this.serializeBrewTypeData(jsonobject);
            return jsonobject;
        }
    }
}
