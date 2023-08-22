package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.BrewParticleProvider;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import caldera.common.network.NetworkHandler;
import caldera.common.network.SpawnBrewParticlesPacket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpawnParticlesActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<SpawnParticlesActionType.SpawnParticlesAction> {

    @Override
    public SpawnParticlesAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        int count = GsonHelper.getAsInt(object, "count");

        if (count <= 0) {
            throw new JsonParseException("Particle count must be greater than 0");
        }

        BrewParticleProvider particle = BrewParticleProvider.deserialize(object);

        return new SpawnParticlesAction(particle, count);
    }

    public static SpawnParticlesAction spawnParticles(BrewParticleProvider particle, int count) {
        return new SpawnParticlesAction(particle, count);
    }

    public static final class SpawnParticlesAction extends SimpleAction {

        private final BrewParticleProvider particle;
        private final int count;

        public SpawnParticlesAction(BrewParticleProvider particle, int count) {
            this.particle = particle;
            this.count = count;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.SPAWN_PARTICLES.get();
        }

        @Override
        public void accept(GenericBrew brew) {
            Level level = brew.getCauldron().getLevel();
            BlockPos pos = brew.getCauldron().getBlockPos();
            if (level != null) {
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)),
                        new SpawnBrewParticlesPacket(pos, count, particle)
                );
            }
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("count", count);
            particle.serialize(object);
        }
    }
}
