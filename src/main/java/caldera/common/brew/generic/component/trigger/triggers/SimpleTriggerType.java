package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.init.CalderaRegistries;
import com.google.gson.JsonObject;

public final class SimpleTriggerType extends TriggerType<SimpleTriggerType.SimpleTrigger> {

    public void trigger(GenericBrew brew) {
        trigger(brew, trigger -> true);
    }

    @Override
    public SimpleTrigger deserialize(JsonObject object) {
        return new SimpleTrigger();
    }

    public SimpleTrigger create() {
        return new SimpleTrigger();
    }

    public class SimpleTrigger implements Trigger {

        @Override
        public TriggerType<?> getType() {
            return CalderaRegistries.TRIGGER_TYPES.getValue(getRegistryName());
        }

        @Override
        public void serialize(JsonObject object) {

        }
    }
}
