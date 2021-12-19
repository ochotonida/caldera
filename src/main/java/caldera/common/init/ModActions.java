package caldera.common.init;

import caldera.Caldera;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.actions.*;
import caldera.common.brew.generic.component.action.actions.conversion.ConvertEntitiesActionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModActions {

    public static final DeferredRegister<ActionType<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.ACTION_TYPES, Caldera.MODID);

    public static final RegistryObject<ExplodeActionType> EXPLODE = REGISTRY.register("explode", ExplodeActionType::new);
    public static final RegistryObject<DestroyCauldronActionType> DESTROY_CAULDRON = REGISTRY.register("destroy_cauldron", DestroyCauldronActionType::new);
    public static final RegistryObject<PlaySoundActionType> PLAY_SOUND = REGISTRY.register("play_sound", PlaySoundActionType::new);
    public static final RegistryObject<ChangeColorActionType> CHANGE_COLOR = REGISTRY.register("change_color", ChangeColorActionType::new);
    public static final RegistryObject<SpawnParticlesActionType> SPAWN_PARTICLES = REGISTRY.register("spawn_particles", SpawnParticlesActionType::new);
    public static final RegistryObject<ConvertEntitiesActionType> CONVERT_ENTITIES = REGISTRY.register("convert_entities", ConvertEntitiesActionType::new);
    public static final RegistryObject<SpawnItemsAction> SPAWN_ITEMS = REGISTRY.register("spawn_items", SpawnItemsAction::new);
}
