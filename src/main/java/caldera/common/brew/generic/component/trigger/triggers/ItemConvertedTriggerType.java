package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.brew.generic.component.trigger.Triggers;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ItemConvertedTriggerType extends TriggerType<ItemConvertedTriggerType.ItemConvertedTrigger> {

    public void trigger(GenericBrew brew, String identifier, ItemStack input, ItemStack result) {
        trigger(brew, trigger -> trigger.matches(identifier, input, result));
    }

    @Override
    public ItemConvertedTrigger deserialize(JsonObject object) {
        String identifier = null;
        if (object.has("identifier")) {
            identifier = GsonHelper.getAsString(object, "identifier");
        }
        ItemPredicate item = ItemPredicate.fromJson(object.get("item"));
        ItemPredicate result = ItemPredicate.fromJson(object.get("result"));
        return new ItemConvertedTrigger(identifier, item, result);
    }

    public static ItemConvertedTrigger itemConverted(@Nullable String identifier, ItemPredicate item, ItemPredicate result) {
        return new ItemConvertedTrigger(identifier, item, result);
    }

    public record ItemConvertedTrigger(@Nullable String identifier, ItemPredicate itemPredicate, ItemPredicate resultPredicate) implements Trigger {

        private boolean matches(String identifier, ItemStack input, ItemStack result) {
            return (this.identifier == null || this.identifier.equals(identifier))
                    && itemPredicate.matches(input)
                    && resultPredicate.matches(result);
        }

        @Override
        public TriggerType<?> getType() {
            return Triggers.ITEM_CONVERTED.get();
        }

        @Override
        public void serialize(JsonObject object) {
            if (identifier != null) {
                object.addProperty("identifier", identifier);
            }
            object.add("item", itemPredicate.serializeToJson());
            object.add("result", resultPredicate.serializeToJson());
        }
    }
}
