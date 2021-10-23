package caldera.common.brew.generic.component.effect;

import caldera.common.brew.generic.component.GenericBrewTypeComponentRegistry;
import caldera.common.brew.generic.component.effect.effects.TimerEffectType;

public class EffectProviders {

    public static final GenericBrewTypeComponentRegistry<EffectProvider, EffectProviderType<?>> EFFECTS = new GenericBrewTypeComponentRegistry<>("effect");

    public static final TimerEffectType TIMER = EFFECTS.register(new TimerEffectType());
}
