package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.init.ModTriggers;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemConsumedTriggerType extends TriggerType<ItemConsumedTriggerType.ItemConsumedTrigger> {

    public void trigger(GenericBrew brew, String identifier, ItemStack input) {
        trigger(brew, trigger -> trigger.matches(identifier, input));
    }

    @Override
    public ItemConsumedTrigger deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        String identifier = null;
        if (object.has("identifier")) {
            identifier = GsonHelper.getAsString(object, "identifier");
        }
        ItemPredicate item = ItemPredicate.fromJson(object.get("item"));
        return new ItemConsumedTrigger(identifier, item);
    }

    public static ItemConsumedTrigger itemConverted(@Nullable String identifier, ItemPredicate item) {
        return new ItemConsumedTrigger(identifier, item);
    }

    public record ItemConsumedTrigger(@Nullable String identifier, ItemPredicate itemPredicate) implements Trigger {

        private boolean matches(String identifier, ItemStack input) {
            return (this.identifier == null || this.identifier.equals(identifier)) && itemPredicate.matches(input);
        }

        @Override
        public TriggerType<?> getType() {
            return ModTriggers.ITEM_CONSUMED.get();
        }

        @Override
        public void serialize(JsonObject object) {
            if (identifier != null) {
                object.addProperty("identifier", identifier);
            }
            object.add("item", itemPredicate.serializeToJson());
        }
    }
}
