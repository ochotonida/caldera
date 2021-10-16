package caldera.common.init;

import caldera.Caldera;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Caldera.MODID);

    public static final RegistryObject<SimpleParticleType> CAULDRON_BUBBLE = REGISTRY.register("cauldron_bubble", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CAULDRON_SPLASH = REGISTRY.register("cauldron_splash", () -> new SimpleParticleType(false));
}
