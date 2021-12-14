package caldera.common.brew.generic.component.trigger;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.generic.GenericBrew;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class TriggerHandler<TRIGGER extends Trigger> {

    private final List<Event<TRIGGER>> events = new ArrayList<>();
    private final TriggerType<TRIGGER> triggerType;

    public TriggerHandler(TriggerType<TRIGGER> triggerType) {
        this.triggerType = triggerType;
    }

    public void addListener(Trigger trigger, List<String> actions) {
        if (!trigger.getType().equals(triggerType.getRegistryName())) {
            throw new IllegalArgumentException("Trigger has incorrect type %s, expected %s".formatted(trigger.getType(), triggerType.getRegistryName()));
        }
        // noinspection unchecked
        events.add(new Event<>((TRIGGER) trigger, actions));
    }

    public void trigger(GenericBrew brew, Predicate<TRIGGER> predicate) {
        if (brew.getCauldron().getLevel() == null || brew.getCauldron().getLevel().isClientSide()) {
            return;
        }
        Cauldron cauldron = brew.getCauldron();

        loop:
        for (Event<TRIGGER> event : events) {
            if (predicate.test(event.trigger())) {
                for (String identifier : event.actions()) {
                    if (cauldron.isRemoved() || cauldron.getBrew() != brew) {
                        break loop;
                    }
                    brew.executeAction(identifier);
                }
            }
        }
    }

    private record Event<TRIGGER extends Trigger>(TRIGGER trigger, List<String> actions)  { }
}
