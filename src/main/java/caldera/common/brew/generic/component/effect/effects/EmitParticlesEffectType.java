package caldera.common.brew.generic.component.effect.effects;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.BrewParticleProvider;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.effect.EffectProviders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EmitParticlesEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<EmitParticlesEffectType.EmitParticlesEffectProvider> {

    @Override
    public EmitParticlesEffectProvider deserialize(JsonObject object) {
        double count = GsonHelper.getAsDouble(object, "count");

        if (count <= 0) {
            throw new JsonParseException("Particle count must be greater than 0");
        }

        BrewParticleProvider particle = BrewParticleProvider.deserialize(object);

        return new EmitParticlesEffectProvider(particle, count);
    }

    @Override
    public EmitParticlesEffectProvider deserialize(FriendlyByteBuf buffer) {
        double count = buffer.readFloat();
        BrewParticleProvider particle = BrewParticleProvider.deserialize(buffer);
        return new EmitParticlesEffectProvider(particle, count);
    }

    public static EmitParticlesEffectProvider emitParticles(BrewParticleProvider particle, double count) {
        return new EmitParticlesEffectProvider(particle, count);
    }

    public static final class EmitParticlesEffectProvider extends EffectProvider {

        private final BrewParticleProvider particle;
        private final double count;

        public EmitParticlesEffectProvider(BrewParticleProvider particle, double count) {
            this.particle = particle;
            this.count = count;
        }

        @Override
        public EffectProviderType<?> getType() {
            return EffectProviders.PARTICLE_EMITTER.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("count", count);
            particle.serialize(object);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeDouble(count);
            particle.serialize(buffer);
        }

        @Override
        public Effect create(GenericBrew brew) {
            return new EmitParticlesEffect(brew);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag) {
            return new EmitParticlesEffect(brew);
        }

        public class EmitParticlesEffect implements Effect {

            private final GenericBrew brew;

            public EmitParticlesEffect(GenericBrew brew) {
                this.brew = brew;
            }

            @Override
            public void tick() {
                if (brew.getCauldron().getLevel() == null) {
                    return;
                }

                if (count < 1) {
                    if (brew.getCauldron().getLevel().getRandom().nextDouble() < count) {
                        particle.spawnParticles(brew, 1);
                    }
                } else {
                    particle.spawnParticles(brew, (int) (count + 0.0005));
                }
            }

            @Override
            public void save(CompoundTag tag) {

            }
        }
    }
}
