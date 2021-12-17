package caldera.common.brew.generic.component.action.actions;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.brew.generic.component.trigger.EntityPredicateHelper;
import caldera.common.init.ModActions;
import caldera.common.init.ModRecipeTypes;
import caldera.common.recipe.conversion.AbstractEntityConversionRecipe;
import caldera.common.recipe.conversion.ConversionRecipeHelper;
import caldera.common.util.CraftingHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ConvertEntitiesActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<ConvertEntitiesActionType.ConvertEntitiesAction> {

    @Override
    public ConvertEntitiesAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation conversionType = CraftingHelper.readResourceLocation(object, "conversionType");

        int maxConverted = -1;
        if (object.has("maxConverted")) {
            maxConverted = GsonHelper.getAsInt(object, "maxConverted");
            if (maxConverted <= 0) {
                throw new JsonParseException("Maximum entities converted must be greater than 0");
            }
        }

        double range = GsonHelper.getAsDouble(object, "range");
        if (range <= 0) {
            throw new JsonParseException("Conversion range must be greater than 0 blocks");
        } else if (range > 16) {
            throw new JsonParseException("Conversion range must be smaller than 16 blocks");
        }

        EntityPredicate.Composite predicate = EntityPredicateHelper.fromJson(object, "entity", context);

        return new ConvertEntitiesAction(conversionType, predicate, maxConverted, range);
    }

    @Override
    public ConvertEntitiesAction deserialize(FriendlyByteBuf buffer) {
        return new ConvertEntitiesAction(null, null, 0, 0); // TODO make this nullable
    }

    public static ConvertEntitiesAction convert(ResourceLocation conversionType, double range) {
        return convert(conversionType, EntityPredicate.Composite.ANY, range);
    }

    public static ConvertEntitiesAction convert(ResourceLocation conversionType, EntityPredicate.Composite predicate, double range) {
        return convert(conversionType, predicate, -1, range);
    }

    public static ConvertEntitiesAction convert(ResourceLocation conversionType, EntityPredicate.Composite predicate, int maxConverted, double range) {
        return new ConvertEntitiesAction(conversionType, predicate, maxConverted, range);
    }

    public static class ConvertEntitiesAction extends SimpleAction {

        private final ConversionRecipeHelper<LivingEntity, AbstractEntityConversionRecipe> conversionHelper;
        private final int maxConverted;
        private final double range;
        private final EntityPredicate.Composite predicate;

        public ConvertEntitiesAction(ResourceLocation conversionType, EntityPredicate.Composite predicate, int maxConverted, double range) {
            this.conversionHelper = new ConversionRecipeHelper<>(ModRecipeTypes.ENTITY_CONVERSION, conversionType);
            this.predicate = predicate;
            this.maxConverted = maxConverted;
            this.range = range;
        }

        @Override
        public void execute(GenericBrew brew) {
            if (!(brew.getCauldron().getLevel() instanceof ServerLevel level)) {
                return;
            }
            Cauldron cauldron = brew.getCauldron();
            Predicate<Entity> combinedPredicate = entity -> entity instanceof LivingEntity && predicate.matches(EntityPredicateHelper.createContext(level, cauldron, entity));
            List<Entity> entities = brew.getCauldron().getEntitiesInRange(range, combinedPredicate);
            Collections.shuffle(entities);

            List<LivingEntity> convertedEntities = new ArrayList<>();

            int entitiesRemaining = maxConverted;
            for (Entity entity : entities) {
                LivingEntity livingEntity = (LivingEntity) entity;

                Optional<AbstractEntityConversionRecipe> recipe = conversionHelper.findMatchingRecipe(level.getRecipeManager(), livingEntity);

                if (recipe.isEmpty()) {
                    continue;
                }

                convertedEntities.add(recipe.get().convertEntity(level, livingEntity));

                // TODO converted_entities trigger

                if (maxConverted != -1 && --entitiesRemaining <= 0) {
                    break;
                }
            }
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.CONVERT_ENTITIES.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("conversionType", conversionHelper.getConversionType().toString());
            if (maxConverted != -1) {
                object.addProperty("maxConverted", maxConverted);
            }
            object.addProperty("range", range);
            object.add("entity", predicate.toJson(SerializationContext.INSTANCE));
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {

        }
    }
}
