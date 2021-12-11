package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class TimerTriggerType extends TriggerType<TimerTriggerType.TimerTrigger> {

    public void trigger(GenericBrew brew, String identifier) {
        trigger(brew, trigger -> identifier.equals(trigger.identifier));
    }

    @Override
    public TimerTrigger deserialize(JsonObject object) {
        String identifier = GsonHelper.getAsString(object, "identifier");
        return new TimerTrigger(identifier);
    }

    public class TimerTrigger implements Trigger {

        private final String identifier;

        public TimerTrigger(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("identifier", identifier);
        }
    }
}
