package caldera.common.brew.generic.component.trigger;

import caldera.common.brew.BrewTypeDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nullable;

public class EntityPredicateHelper {

    public static EntityPredicate.Composite fromJson(JsonObject object, String memberName, BrewTypeDeserializationContext context) {
        JsonElement jsonelement = object.get(memberName);
        return fromElement(memberName, context, jsonelement);
    }

    public static EntityPredicate.Composite[] fromJsonArray(JsonObject object, String memberName, BrewTypeDeserializationContext context) {
        JsonElement jsonelement = object.get(memberName);
        if (jsonelement != null && !jsonelement.isJsonNull()) {
            JsonArray array = GsonHelper.convertToJsonArray(jsonelement, memberName);
            EntityPredicate.Composite[] predicate = new EntityPredicate.Composite[array.size()];

            for (int i = 0; i < array.size(); ++i) {
                predicate[i] = fromElement(memberName + "[" + i + "]", context, array.get(i));
            }

            return predicate;
        } else {
            return new EntityPredicate.Composite[0];
        }
    }

    private static EntityPredicate.Composite fromElement(String name, BrewTypeDeserializationContext context, @Nullable JsonElement element) {
        if (element != null && element.isJsonArray()) {
            LootItemCondition[] conditions = context.deserializeConditions(element.getAsJsonArray(), context.getBrewType() + "/" + name, LootContextParamSets.ADVANCEMENT_ENTITY);
            return EntityPredicate.Composite.create(conditions);
        } else {
            EntityPredicate predicate = EntityPredicate.fromJson(element);
            return EntityPredicate.Composite.wrap(predicate);
        }
    }
}
