package caldera.common.brew.generic.component.action;

import caldera.Caldera;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.actions.*;
import caldera.common.init.CalderaRegistries;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class Actions {

    public static final DeferredRegister<ActionType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.ACTION_TYPES, Caldera.MODID);

    public static final RegistryObject<EffectActionType> START_EFFECT = REGISTRY.register("start_effect", () -> new EffectActionType(GenericBrew::startEffect));
    public static final RegistryObject<EffectActionType> REMOVE_EFFECT = REGISTRY.register("remove_effect", () -> new EffectActionType(GenericBrew::removeEffect));
    public static final RegistryObject<ExplodeActionType> EXPLODE = REGISTRY.register("explode", ExplodeActionType::new);
    public static final RegistryObject<PlaySoundActionType> PLAY_SOUND = REGISTRY.register("play_sound", PlaySoundActionType::new);
    public static final RegistryObject<ChangeColorActionType> CHANGE_COLOR = REGISTRY.register("change_color", ChangeColorActionType::new);
    public static final RegistryObject<SpawnParticlesActionType> SPAWN_PARTICLES = REGISTRY.register("spawn_particles", SpawnParticlesActionType::new);
}
