package caldera.common.init;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class CalderaRegistries {

    public static IForgeRegistry<BrewTypeSerializer<?>> BREW_TYPE_SERIALIZERS;

    @SuppressWarnings("unused")
    public static void onNewRegistry(RegistryEvent.NewRegistry event) {
        BREW_TYPE_SERIALIZERS = new RegistryBuilder<BrewTypeSerializer<?>>()
                .setType(magic(BrewTypeSerializer.class))
                .setName(new ResourceLocation(Caldera.MODID, "brew_types"))
                .create();

        ModBrewTypes.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Class<T> magic(Class<?> cls) {
        // noinspection unchecked
        return (Class<T>) cls;
    }
}
