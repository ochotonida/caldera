package caldera.common.recipe.brew.generic.component;

import caldera.common.recipe.brew.generic.component.trigger.TriggerHandler;
import caldera.common.recipe.brew.generic.component.trigger.Trigger;
import caldera.common.recipe.brew.generic.component.trigger.TriggerType;
import caldera.common.recipe.brew.generic.component.trigger.Triggers;

import java.util.HashSet;
import java.util.Set;

public class ActionHandler {

    private final Set<TriggerHandler<?>> triggers = new HashSet<>();

    private ActionHandler() { }

    public static ActionHandler create() {
        ActionHandler result = new ActionHandler();
        for (TriggerType<?> triggerType : Triggers.TRIGGERS) {
            result.triggers.add(new TriggerHandler<>(triggerType));
        }
        return result;
    }

    public <INSTANCE extends Trigger> TriggerHandler<INSTANCE> getTrigger(TriggerType<INSTANCE> triggerType) {
        for (TriggerHandler<?> trigger : triggers) {
            if (trigger.getTriggerType() == triggerType) {
                // noinspection unchecked
                return (TriggerHandler<INSTANCE>) trigger;
            }
        }
        throw new IllegalArgumentException();
    }
}
