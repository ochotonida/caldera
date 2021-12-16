package caldera.common.brew.generic.component.trigger;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import com.google.gson.JsonObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Predicate;

public abstract class TriggerType<TRIGGER extends Trigger> extends ForgeRegistryEntry<TriggerType<?>> {

    public abstract TRIGGER deserialize(JsonObject object, BrewTypeDeserializationContext context);

    protected void trigger(GenericBrew brew, Predicate<TRIGGER> predicate) {
        TriggerHandler<TRIGGER> triggerHandler = brew.getType().getTrigger(this);
        if (triggerHandler != null) {
            triggerHandler.trigger(brew, predicate);
        }
    }
}
