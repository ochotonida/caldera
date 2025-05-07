package caldera.registry;

import caldera.Caldera;
import caldera.block.cauldron.contents.CauldronContents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModRegistries {

    public static final ResourceKey<Registry<CauldronContents.Type<?>>> CAULDRON_CONTENT_TYPE = ResourceKey.createRegistryKey(Caldera.id("cauldron_contents"));
    public static final Registry<CauldronContents.Type<?>> CAULDRON_CONTENT_TYPES = new RegistryBuilder<>(CAULDRON_CONTENT_TYPE).sync(true).create();

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(CAULDRON_CONTENT_TYPES);
    }
}
