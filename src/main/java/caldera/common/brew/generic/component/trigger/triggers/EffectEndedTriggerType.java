package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.brew.generic.component.trigger.Triggers;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public class EffectEndedTriggerType extends TriggerType<EffectEndedTriggerType.EffectEndedTrigger> {

    public void trigger(GenericBrew brew, String identifier) {
        trigger(brew, trigger -> identifier.equals(trigger.identifier));
    }

    @Override
    public EffectEndedTrigger deserialize(JsonObject object) {
        String identifier = GsonHelper.getAsString(object, "identifier");
        return new EffectEndedTrigger(identifier);
    }

    public static EffectEndedTrigger effectEnded(String identifier) {
        return new EffectEndedTrigger(identifier);
    }

    public record EffectEndedTrigger(String identifier) implements Trigger {

        @Override
        public TriggerType<?> getType() {
            return Triggers.EFFECT_ENDED.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("identifier", identifier);
        }
    }
}
