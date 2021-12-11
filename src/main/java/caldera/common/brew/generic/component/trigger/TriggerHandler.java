package caldera.common.brew.generic.component.trigger;

import caldera.common.brew.generic.GenericBrew;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TriggerHandler<INSTANCE extends Trigger> {

    private final Map<INSTANCE, List<String>> triggers = new HashMap<>();
    private final TriggerType<INSTANCE> triggerType;

    public TriggerHandler(TriggerType<INSTANCE> triggerType) {
        this.triggerType = triggerType;
    }

    public void addListener(Trigger trigger, List<String> actions) {
        if (!trigger.getType().equals(triggerType.getRegistryName())) {
            throw new IllegalArgumentException("Trigger has incorrect type %s, expected %s".formatted(trigger.getType(), triggerType.getRegistryName()));
        }
        // noinspection unchecked
        triggers.put((INSTANCE) trigger, actions);
    }

    public void trigger(GenericBrew brew, Predicate<INSTANCE> predicate) {
        if (brew.getCauldron().getLevel() != null && brew.getCauldron().getLevel().isClientSide()) {
            return; // TODO send to clients
        }

        triggers.forEach((trigger, actions) -> {
            if (predicate.test(trigger)) {
                actions.forEach(identifier -> brew.getType().getAction(identifier).accept(brew));
            }
        });
    }
}
