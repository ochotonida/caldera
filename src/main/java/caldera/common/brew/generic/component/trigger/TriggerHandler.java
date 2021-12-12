package caldera.common.brew.generic.component.trigger;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.recipe.Cauldron;

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
        if (brew.getCauldron().getLevel() != null && brew.getCauldron().getLevel().isClientSide()) {
            return; // TODO send to clients
        }
        Cauldron cauldron = brew.getCauldron();

        for (Event<TRIGGER> event : events) {
            if (predicate.test(event.trigger())) {
                for (String identifier : event.actions()) {
                    if (cauldron.isRemoved() || cauldron.getBrew() != brew) {
                        break;
                    }
                    brew.getType().getAction(identifier).accept(brew);
                }
            }
            if (cauldron.isRemoved() || cauldron.getBrew() != brew) {
                break;
            }
        }
    }

    private record Event<TRIGGER extends Trigger>(TRIGGER trigger, List<String> actions)  { }
}
