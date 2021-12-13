package caldera.common.brew.generic.component.action.actions;

import caldera.Caldera;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.Actions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpawnParticlesActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<SpawnParticlesActionType.SpawnParticlesAction> {

    @Override
    public boolean shouldSendToClients() {
        return true;
    }

    @Override
    public SpawnParticlesAction deserialize(JsonObject object) {
        if (!object.has("particle")) {
            throw new JsonParseException("Missing 'particle'");
        }
        ParticleOptions particle = ParticleTypes.CODEC
                .decode(JsonOps.INSTANCE, object.get("particle"))
                .resultOrPartial(Caldera.LOGGER::error)
                .orElseThrow(() -> {
                    throw new JsonParseException("Failed to parse particle: " + object.get("particle"));
                })
                .getFirst();
        int count = GsonHelper.getAsInt(object, "count");

        boolean colored = false;
        if (object.has("useBrewColor")) {
            colored = GsonHelper.getAsBoolean(object, "useBrewColor");
        }

        return new SpawnParticlesAction(particle, count, colored);
    }

    @Override
    public SpawnParticlesAction deserialize(FriendlyByteBuf buffer) {
        ParticleOptions particle = ParticleTypes.CODEC
                .decode(NbtOps.INSTANCE, buffer.readNbt())
                .resultOrPartial(Caldera.LOGGER::error)
                .orElseThrow()
                .getFirst();
        int count = buffer.readInt();
        boolean useBrewColor = buffer.readBoolean();

        return new SpawnParticlesAction(particle, count, useBrewColor);
    }

    public SpawnParticlesAction spawnParticles(ParticleOptions particle, int count, boolean colored) {
        return new SpawnParticlesAction(particle, count, colored);
    }

    // TODO add better control over color/speed parameters
    // TODO add height parameter (absolute/relative)
    // TODO add x/y location parameter
    public record SpawnParticlesAction(ParticleOptions particle, int count, boolean useBrewColor) implements Action {

        @Override
        public ActionType<?> getType() {
            return Actions.SPAWN_PARTICLES.get();
        }

        @Override
        public void accept(GenericBrew brew) {
            Level level = brew.getCauldron().getLevel();
            if (level == null || !level.isClientSide()) {
                return;
            }
            brew.getCauldron().spawnParticles(particle, count, useBrewColor ? brew.getColor(0) : 0);
        }

        @Override
        public void serialize(JsonObject object) {
            ParticleTypes.CODEC
                    .encodeStart(JsonOps.INSTANCE, particle())
                    .resultOrPartial(Caldera.LOGGER::error)
                    .ifPresent(element -> object.add("particle", element));
            object.addProperty("count", count);
            if (useBrewColor) {
                object.addProperty("useBrewColor", true);
            }
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            Tag tag = ParticleTypes.CODEC
                    .encodeStart(NbtOps.INSTANCE, particle())
                    .resultOrPartial(Caldera.LOGGER::error)
                    .orElseThrow();

            buffer.writeNbt(((CompoundTag) tag));
            buffer.writeInt(count);
            buffer.writeBoolean(useBrewColor);
        }
    }
}
