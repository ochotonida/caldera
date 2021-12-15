package caldera.common.brew.generic.component.trigger.triggers;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
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

    public EffectEndedTrigger effectEnded(String identifier) {
        return new EffectEndedTrigger(identifier);
    }

    public class EffectEndedTrigger implements Trigger {

        private final String identifier;

        public EffectEndedTrigger(String identifier) {
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
