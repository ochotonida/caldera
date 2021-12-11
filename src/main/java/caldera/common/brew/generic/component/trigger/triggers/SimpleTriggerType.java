package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public final class SimpleTriggerType extends TriggerType<SimpleTriggerType.SimpleTrigger> {

    public void trigger(GenericBrew brew) {
        trigger(brew, trigger -> true);
    }

    @Override
    public SimpleTrigger deserialize(JsonObject object) {
        return new SimpleTrigger();
    }

    public class SimpleTrigger implements Trigger {

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void serialize(JsonObject object) {

        }
    }
}
