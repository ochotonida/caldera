package caldera.common.brew.generic.component;

import caldera.Caldera;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

public record BrewParticleProvider(ParticleOptions particle, boolean useBrewColor) {

    public static final Codec<BrewParticleProvider> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(provider -> provider.particle),
            Codec.BOOL.optionalFieldOf("useBrewColor", false).forGetter(provider -> provider.useBrewColor)
    ).apply(builder, BrewParticleProvider::new));

    // TODO add more control over position & motion
    public void spawnParticles(GenericBrew brew, int amount) {
        Level level = brew.getCauldron().getLevel();
        if (level == null) {
            return;
        }

        double xSpeed = 0;
        double ySpeed = 0;
        double zSpeed = 0;

        if (useBrewColor) {
            int color = brew.getColor(0);

            xSpeed = ColorHelper.getRed(color) / 255D;
            ySpeed = ColorHelper.getGreen(color) / 255D;
            zSpeed = ColorHelper.getBlue(color) / 255D;
        }

        double yOffset = 0.5 / 16D;

        for (int i = 0; i < amount; i++) {
            double xOffset = (level.getRandom().nextDouble() * 2 - 1) * 13 / 16;
            double zOffset = (level.getRandom().nextDouble() * 2 - 1) * 13 / 16;

            brew.getCauldron().spawnParticle(particle, xOffset, yOffset, zOffset, xSpeed, ySpeed, zSpeed, true);
        }
    }

    public static BrewParticleProvider deserialize(JsonObject object) {
        return CODEC.decode(JsonOps.INSTANCE, object)
                .resultOrPartial(Caldera.LOGGER::error)
                .orElseThrow()
                .getFirst();
    }

    public static BrewParticleProvider deserialize(FriendlyByteBuf buffer) {
        return CODEC.decode(NbtOps.INSTANCE, buffer.readNbt())
                .resultOrPartial(Caldera.LOGGER::error)
                .orElseThrow()
                .getFirst();
    }

    public void serialize(JsonObject object) {
        ParticleTypes.CODEC
                .encodeStart(JsonOps.INSTANCE, particle())
                .resultOrPartial(Caldera.LOGGER::error)
                .ifPresent(element -> object.add("particle", element));
        if (useBrewColor) {
            object.addProperty("useBrewColor", true);
        }
    }

    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeNbt((CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow(false, Caldera.LOGGER::error));
    }
}
