package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.Actions;
import com.google.gson.JsonObject;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class SpawnParticlesActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<SpawnParticlesActionType.SpawnParticlesAction> {

    @Override
    public boolean shouldSendToClients() {
        return true;
    }

    @Override
    public SpawnParticlesAction deserialize(JsonObject object) {
        return new SpawnParticlesAction();
    }

    @Override
    public SpawnParticlesAction deserialize(FriendlyByteBuf buffer) {
        return new SpawnParticlesAction();
    }

    public SpawnParticlesAction spawnParticles() {
        return new SpawnParticlesAction();
    }

    // TODO add parameters
    public record SpawnParticlesAction() implements Action {

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
            brew.getCauldron().spawnParticles(ParticleTypes.ENTITY_EFFECT, 50, brew.getColor(0));
        }

        @Override
        public void serialize(JsonObject object) {

        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {

        }
    }
}
