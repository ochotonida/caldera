package caldera.common.brew.generic.component.trigger;

import caldera.Caldera;
import caldera.common.brew.generic.component.trigger.triggers.EffectEndedTriggerType;
import caldera.common.brew.generic.component.trigger.triggers.SimpleTriggerType;
import caldera.common.init.CalderaRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Triggers {

    public static final DeferredRegister<TriggerType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.TRIGGER_TYPES, Caldera.MODID);

    public static final RegistryObject<SimpleTriggerType> BREW_CREATED = REGISTRY.register("brew_created", SimpleTriggerType::new);
    public static final RegistryObject<EffectEndedTriggerType> TIMER = REGISTRY.register("effect_ended", EffectEndedTriggerType::new);
}
