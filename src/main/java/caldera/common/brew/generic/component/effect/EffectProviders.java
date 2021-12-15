package caldera.common.brew.generic.component.effect;

import caldera.Caldera;
import caldera.common.brew.generic.component.effect.effects.ConsumeItemsEffectType;
import caldera.common.brew.generic.component.effect.effects.EmitParticlesEffectType;
import caldera.common.brew.generic.component.effect.effects.TimerEffectType;
import caldera.common.brew.generic.component.effect.effects.conversion.ConvertItemsEffectType;
import caldera.common.init.CalderaRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EffectProviders {

    public static final DeferredRegister<EffectProviderType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.EFFECT_PROVIDER_TYPES, Caldera.MODID);

    public static final RegistryObject<TimerEffectType> TIMER = REGISTRY.register("timer", TimerEffectType::new);
    public static final RegistryObject<EmitParticlesEffectType> PARTICLE_EMITTER = REGISTRY.register("emit_particles", EmitParticlesEffectType::new);
    public static final RegistryObject<ConvertItemsEffectType> CONVERT_ITEMS = REGISTRY.register("convert_items", ConvertItemsEffectType::new);
    public static final RegistryObject<ConsumeItemsEffectType> CONSUME_ITEMS = REGISTRY.register("consume_items", ConsumeItemsEffectType::new);
}
