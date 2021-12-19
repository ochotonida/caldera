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
import net.minecraft.world.entity.player.Player;

public class CauldronBrokenTriggerType extends TriggerType<CauldronBrokenTriggerType.CauldronBrokenTrigger> {

    public void trigger(GenericBrew brew, Player player) {
        Cauldron cauldron = brew.getCauldron();
        ServerLevel level = (ServerLevel) cauldron.getLevel();
        trigger(brew, trigger -> trigger.player().matches(EntityPredicateHelper.createContext(level, cauldron, player)));
    }

    @Override
    public CauldronBrokenTrigger deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        EntityPredicate.Composite playerPredicate = EntityPredicateHelper.fromJson(object, "player", context);
        return new CauldronBrokenTrigger(playerPredicate);
    }

    public static CauldronBrokenTrigger cauldronBroken(EntityPredicate.Composite player) {
        return new CauldronBrokenTrigger(player);
    }

    public record CauldronBrokenTrigger(EntityPredicate.Composite player) implements Trigger {

        @Override
        public TriggerType<?> getType() {
            return ModTriggers.CAULDRON_BROKEN.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.add("player", player.toJson(SerializationContext.INSTANCE));
        }
    }
}
