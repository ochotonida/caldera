package caldera.common.recipe.brew.generic.component.trigger;

import caldera.Caldera;
import caldera.common.recipe.brew.generic.component.GenericBrewTypeComponentRegistry;
import caldera.common.recipe.brew.generic.component.trigger.triggers.SimpleTriggerType;
import caldera.common.recipe.brew.generic.component.trigger.triggers.TimerTriggerType;
import net.minecraft.resources.ResourceLocation;

public class Triggers {

    public static final GenericBrewTypeComponentRegistry<Trigger, TriggerType<?>> TRIGGERS = new GenericBrewTypeComponentRegistry<>("trigger");

    public static final SimpleTriggerType BREW_CREATED = TRIGGERS.register(new SimpleTriggerType(new ResourceLocation(Caldera.MODID, "brew_created")));
    public static final TimerTriggerType TIMER = TRIGGERS.register(new TimerTriggerType());
}
