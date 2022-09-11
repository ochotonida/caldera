package caldera.common.recipe.ingredient;

import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

public record EntityIngredient(EntityType<?> entityType, Tag<EntityType<?>> entityTypeTag) implements Predicate<Entity> {

    public static final EntityIngredient EMPTY = new EntityIngredient(null, null);

    public static EntityIngredient of(EntityType<?> type) {
        if (type == null) {
            throw new NullPointerException();
        }
        return new EntityIngredient(type, null);
    }

    public static EntityIngredient of(Tag<EntityType<?>> tag) {
        if (tag == null) {
            throw new NullPointerException();
        }
        return new EntityIngredient(null, tag);
    }

    @Override
    public boolean test(Entity entity) {
        if (this.entityType != null) {
            return entity.getType() == entityType;
        } else if (entityTypeTag != null) {
            return entity.getType().is(entityTypeTag);
        }
        return false;
    }

    public static EntityIngredient fromJson(JsonObject object, String name) {
        JsonObject ingredient = GsonHelper.getAsJsonObject(object, name);

        if (ingredient.has("tag")) {
            ResourceLocation tagName = CraftingHelper.readResourceLocation(ingredient, "tag");
            Tag<EntityType<?>> fluidTag = SerializationTags.getInstance().getTagOrThrow(Registry.ENTITY_TYPE_REGISTRY, tagName, (id) -> new JsonSyntaxException("Unknown entity type tag '" + id + "'"));
            return of(fluidTag);
        } else if (ingredient.has("type")) {
            ResourceLocation entityId = CraftingHelper.readResourceLocation(ingredient, "type");
            if (!ForgeRegistries.ENTITIES.containsKey(entityId)) {
                throw new JsonParseException("Invalid entity type: " + entityId);
            }
            return EntityIngredient.of(ForgeRegistries.ENTITIES.getValue(entityId));
        }
        throw new JsonParseException("Missing 'type' or 'tag', expected to find a resource location");
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        if (entityType != null) {
            // noinspection ConstantConditions
            result.addProperty("type", entityType.getRegistryName().toString());
        } else {
            ResourceLocation tagName = SerializationTags.getInstance().getIdOrThrow(Registry.ENTITY_TYPE_REGISTRY, entityTypeTag, () -> new IllegalStateException("Unknown fluid tag"));
            result.addProperty("tag", tagName.toString());
        }
        return result;
    }

    public static EntityIngredient fromBuffer(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return EMPTY;
        } else if (buffer.readBoolean()) {
            ResourceLocation entityId = buffer.readResourceLocation();
            return EntityIngredient.of(ForgeRegistries.ENTITIES.getValue(entityId));
        } else {
            ResourceLocation tagName = buffer.readResourceLocation();
            Tag<EntityType<?>> entityTypeTag = SerializationTags.getInstance().getTagOrThrow(Registry.ENTITY_TYPE_REGISTRY, tagName, (id) -> null);
            return of(entityTypeTag);
        }
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        boolean isEmpty = entityType == null && entityTypeTag == null;
        buffer.writeBoolean(isEmpty);
        if (isEmpty) {
            return;
        }
        buffer.writeBoolean(entityType != null);
        if (entityType != null) {
            // noinspection ConstantConditions
            buffer.writeResourceLocation(entityType.getRegistryName());
        } else {
            ResourceLocation tagName = SerializationTags.getInstance().getIdOrThrow(Registry.ENTITY_TYPE_REGISTRY, entityTypeTag, () -> null);
            buffer.writeResourceLocation(tagName);
        }
    }
}
