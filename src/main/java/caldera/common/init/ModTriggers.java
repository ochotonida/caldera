package caldera.common.init;

import caldera.Caldera;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.brew.generic.component.trigger.triggers.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTriggers {

    public static final DeferredRegister<TriggerType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.TRIGGER_TYPES, Caldera.MODID);

    public static final RegistryObject<SimpleTriggerType> BREW_CREATED = REGISTRY.register("brew_created", SimpleTriggerType::new);
    public static final RegistryObject<CauldronBrokenTriggerType> CAULDRON_BROKEN = REGISTRY.register("cauldron_broken", CauldronBrokenTriggerType::new);
    public static final RegistryObject<EffectEndedTriggerType> EFFECT_ENDED = REGISTRY.register("effect_ended", EffectEndedTriggerType::new);
    public static final RegistryObject<ItemConvertedTriggerType> ITEM_CONVERTED = REGISTRY.register("item_converted", ItemConvertedTriggerType::new);
    public static final RegistryObject<ItemConsumedTriggerType> ITEM_CONSUMED = REGISTRY.register("item_consumed", ItemConsumedTriggerType::new);
}
