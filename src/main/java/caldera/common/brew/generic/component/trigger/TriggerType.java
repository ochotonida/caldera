package caldera.common.brew.generic.component.trigger;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.GenericBrewType;
import caldera.common.brew.generic.component.GenericBrewTypeComponent;

import java.util.function.Predicate;

public abstract class TriggerType<TRIGGER extends Trigger> implements GenericBrewTypeComponent<TRIGGER> {

    public TriggerHandler<TRIGGER> getTrigger(GenericBrewType brewType) {
        return brewType.getActions().getTrigger(this);
    }

    protected void trigger(GenericBrew brew, Predicate<TRIGGER> predicate) {
        brew.getType().getActions().getTrigger(this).trigger(brew, predicate);
    }
}
