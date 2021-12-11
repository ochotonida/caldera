package caldera.common.brew.generic.component.trigger;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.GenericBrewType;
import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Predicate;

public abstract class TriggerType<TRIGGER extends Trigger> extends ForgeRegistryEntry<TriggerType<?>> {

    public abstract TRIGGER deserialize(JsonObject object);

    public TriggerHandler<TRIGGER> getTrigger(GenericBrewType brewType) {
        return brewType.getTrigger(this);
    }

    protected void trigger(GenericBrew brew, Predicate<TRIGGER> predicate) {
        TriggerHandler<TRIGGER> triggerHandler = brew.getType().getTrigger(this);
        if (triggerHandler != null) {
            triggerHandler.trigger(brew, predicate);
        }
    }
}
