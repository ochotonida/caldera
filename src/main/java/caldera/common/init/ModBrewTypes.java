package caldera.common.init;

import caldera.Caldera;
import caldera.common.brew.BrewTypeSerializer;
import caldera.common.brew.generic.GenericBrewType;
import caldera.common.brew.sludge.SludgeBrewType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModBrewTypes {

    public static final DeferredRegister<BrewTypeSerializer<?>> REGISTRY = DeferredRegister.create(CalderaRegistries.BREW_TYPE_SERIALIZERS, Caldera.MODID);

    public static final RegistryObject<GenericBrewType.Serializer> GENERIC_BREW_SERIALIZER = REGISTRY.register("generic_brew", GenericBrewType.Serializer::new);
    public static final RegistryObject<SludgeBrewType.Serializer> SLUDGE_BREW_SERIALIZER = REGISTRY.register("sludge", SludgeBrewType.Serializer::new);
}
