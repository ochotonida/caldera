package caldera.common.brew;

import caldera.Caldera;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class BrewTypeDeserializationContext {

    private final ResourceLocation id;
    private final PredicateManager predicateManager;
    private final Gson predicateGson = Deserializers.createConditionSerializer().create();

    public BrewTypeDeserializationContext(ResourceLocation id, PredicateManager predicateManager) {
        this.predicateManager = predicateManager;
        this.id = id;
    }

    public final LootItemCondition[] deserializeConditions(JsonArray array, String triggerName, LootContextParamSet parameterSet) {
        LootItemCondition[] conditions = predicateGson.fromJson(array, LootItemCondition[].class);
        ValidationContext context = new ValidationContext(parameterSet, predicateManager::get, (lootTableId) -> null);

        for (LootItemCondition condition : conditions) {
            condition.validate(context);
            context.getProblems().forEach(
                    (conditionName, problem) -> Caldera.LOGGER.warn("Found validation problem in brew trigger {}: {}", triggerName, problem)
            );
        }

        return conditions;
    }

    public ResourceLocation getBrewType() {
        return this.id;
    }
}
