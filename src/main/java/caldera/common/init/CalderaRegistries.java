package caldera.common.init;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.Actions;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.brew.generic.component.trigger.TriggerType;
import caldera.common.brew.generic.component.trigger.Triggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class CalderaRegistries {

    public static IForgeRegistry<BrewTypeSerializer<?>> BREW_TYPE_SERIALIZERS;
    public static IForgeRegistry<TriggerType<?>> TRIGGER_TYPES;
    public static IForgeRegistry<ActionType<?>> ACTION_TYPES;
    public static IForgeRegistry<EffectProviderType<?>> EFFECT_PROVIDER_TYPES;

    @SuppressWarnings("unused")
    public static void onNewRegistry(RegistryEvent.NewRegistry event) {
        BREW_TYPE_SERIALIZERS = new RegistryBuilder<BrewTypeSerializer<?>>()
                .setType(magic(BrewTypeSerializer.class))
                .setName(new ResourceLocation(Caldera.MODID, "brew_types"))
                .create();
        TRIGGER_TYPES = new RegistryBuilder<TriggerType<?>>()
                .setType(magic(TriggerType.class))
                .setName(new ResourceLocation(Caldera.MODID, "brew_trigger_types"))
                .create();
        ACTION_TYPES = new RegistryBuilder<ActionType<?>>()
                .setType(magic(ActionType.class))
                .setName(new ResourceLocation(Caldera.MODID, "brew_action_types"))
                .create();
        EFFECT_PROVIDER_TYPES = new RegistryBuilder<EffectProviderType<?>>()
                .setType(magic(EffectProviderType.class))
                .setName(new ResourceLocation(Caldera.MODID, "brew_effect_provider_types"))
                .create();

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBrewTypes.REGISTRY.register(modBus);
        Actions.REGISTRY.register(modBus);
        EffectProviders.REGISTRY.register(modBus);
        Triggers.REGISTRY.register(modBus);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Class<T> magic(Class<?> cls) {
        // noinspection unchecked
        return (Class<T>) cls;
    }
}
