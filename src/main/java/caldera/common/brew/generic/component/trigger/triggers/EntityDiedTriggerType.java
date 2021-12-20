package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.EntityPredicateHelper;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.init.ModTriggers;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;

import javax.annotation.Nullable;

public class EntityDiedTriggerType extends TriggerType<EntityDiedTriggerType.EntityDiedTrigger> {

    public void trigger(GenericBrew brew, LivingEntity entity, @Nullable LivingEntity killer) {
        Cauldron cauldron = brew.getCauldron();
        if (!(cauldron.getLevel() instanceof ServerLevel level)) {
            return;
        }
        LootContext entityContext = EntityPredicateHelper.createContext(level, cauldron, entity);
        LootContext killerContext = killer == null ? null : EntityPredicateHelper.createContext(level, cauldron, killer);
        trigger(brew, trigger -> trigger.matches(entityContext, killerContext));
    }

    @Override
    public EntityDiedTrigger deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        EntityPredicate.Composite entityPredicate = EntityPredicateHelper.fromJson(object, "entity", context);
        EntityPredicate.Composite killerPredicate = EntityPredicateHelper.fromJson(object, "killer", context);
        return new EntityDiedTrigger(entityPredicate, killerPredicate);
    }

    public static EntityDiedTrigger entityDied(EntityPredicate entity, EntityPredicate killer) {
        return entityDied(EntityPredicate.Composite.wrap(entity), EntityPredicate.Composite.wrap(killer));
    }

    public static EntityDiedTrigger entityDied(EntityPredicate.Composite entity, EntityPredicate.Composite killer) {
        return new EntityDiedTrigger(entity, killer);
    }

    public record EntityDiedTrigger(EntityPredicate.Composite entity, EntityPredicate.Composite killer) implements Trigger {

        public boolean matches(LootContext entityContext, @Nullable LootContext killerContext) {
            if (!entity.matches(entityContext)) {
                return false;
            }
            if (killer == EntityPredicate.Composite.ANY) {
                return true;
            }
            return killerContext != null && killer.matches(killerContext);
        }

        @Override
        public TriggerType<?> getType() {
            return ModTriggers.ENTITY_DIED.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.add("entity", entity.toJson(SerializationContext.INSTANCE));
            object.add("killer", killer.toJson(SerializationContext.INSTANCE));
        }
    }
}
