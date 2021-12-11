package caldera.common.brew.generic.component.trigger;

import caldera.Caldera;
import caldera.common.brew.generic.component.trigger.triggers.SimpleTriggerType;
import caldera.common.brew.generic.component.trigger.triggers.TimerTriggerType;
import caldera.common.init.CalderaRegistries;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class Triggers {

    public static final DeferredRegister<TriggerType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.TRIGGER_TYPES, Caldera.MODID);

    public static final RegistryObject<SimpleTriggerType> BREW_CREATED = REGISTRY.register("brew_created", SimpleTriggerType::new);
    public static final RegistryObject<TimerTriggerType> TIMER = REGISTRY.register("timer", TimerTriggerType::new);
}
