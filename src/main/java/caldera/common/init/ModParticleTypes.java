package caldera.common.init;

import caldera.Caldera;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Caldera.MODID);

    public static final RegistryObject<BasicParticleType> CAULDRON_BUBBLE = REGISTRY.register("cauldron_bubble", () -> new BasicParticleType(false));
}
