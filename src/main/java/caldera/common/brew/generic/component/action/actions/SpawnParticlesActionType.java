package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.BrewParticleProvider;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
    public SpawnParticlesAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        int count = GsonHelper.getAsInt(object, "count");

        if (count <= 0) {
            throw new JsonParseException("Particle count must be greater than 0");
        }

        BrewParticleProvider particle = BrewParticleProvider.deserialize(object);

        return new SpawnParticlesAction(particle, count);
    }

    @Override
    public SpawnParticlesAction deserialize(FriendlyByteBuf buffer) {
        int count = buffer.readInt();

        BrewParticleProvider particle = BrewParticleProvider.deserialize(buffer);

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
        public void execute(GenericBrew brew) {
            Level level = brew.getCauldron().getLevel();
            if (level == null || !level.isClientSide()) {
                return;
            }
            for (int i = 0; i < count; i++) {
                particle.spawnParticles(brew, 1);
            }
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("count", count);
            particle.serialize(object);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeInt(count);
            particle.serialize(buffer);
        }
    }
}
