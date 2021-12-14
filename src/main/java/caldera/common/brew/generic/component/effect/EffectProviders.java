package caldera.common.brew.generic.component.effect;

import caldera.Caldera;
import caldera.common.brew.generic.component.effect.effects.ParticleEmitterEffectType;
import caldera.common.brew.generic.component.effect.effects.TimerEffectType;
import caldera.common.init.CalderaRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EffectProviders {

    public static final DeferredRegister<EffectProviderType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.EFFECT_PROVIDER_TYPES, Caldera.MODID);

    public static final RegistryObject<TimerEffectType> TIMER = REGISTRY.register("timer", TimerEffectType::new);
    public static final RegistryObject<ParticleEmitterEffectType> PARTICLE_EMITTER = REGISTRY.register("particle_emitter", ParticleEmitterEffectType::new);
}
