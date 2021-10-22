package caldera.common.recipe.brew.generic.component.trigger;

import caldera.common.recipe.brew.generic.GenericBrew;
import caldera.common.recipe.brew.generic.component.action.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TriggerHandler<INSTANCE extends Trigger> {

    private final Map<INSTANCE, List<Action>> triggers = new HashMap<>();
    private final TriggerType<INSTANCE> triggerType;

    public TriggerHandler(TriggerType<INSTANCE> triggerType) {
        this.triggerType = triggerType;
    }

    public TriggerType<INSTANCE> getTriggerType() {
        return triggerType;
    }

    public void addListener(INSTANCE triggerInstance, List<Action> actions) {
        triggers.put(triggerInstance, actions);
    }

    public void removeListener(INSTANCE triggerInstance) {
        triggers.remove(triggerInstance);
    }

    public void trigger(GenericBrew brew, Predicate<INSTANCE> predicate) {
        triggers.forEach((trigger, actions) -> {
            if (predicate.test(trigger)) {
                actions.forEach(action -> action.accept(brew));
            }
        });
    }
}
