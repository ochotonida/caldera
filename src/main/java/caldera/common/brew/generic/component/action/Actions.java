package caldera.common.brew.generic.component.action;

import caldera.Caldera;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.GenericBrewTypeComponentRegistry;
import caldera.common.brew.generic.component.action.actions.EffectActionType;
import net.minecraft.resources.ResourceLocation;

public class Actions {

    public static final GenericBrewTypeComponentRegistry<Action, ActionType<?>> ACTIONS = new GenericBrewTypeComponentRegistry<>("action");

    public static final EffectActionType START_EFFECT = ACTIONS.register(new EffectActionType(new ResourceLocation(Caldera.MODID, "start_effect"), GenericBrew::startEffect));
    public static final EffectActionType REMOVE_EFFECT = ACTIONS.register(new EffectActionType(new ResourceLocation(Caldera.MODID, "remove_effect"), GenericBrew::removeEffect));
}
