package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.recipe.Cauldron;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Locale;

public class ExplodeActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<ExplodeActionType.ExplodeAction> {

    @Override
    public ExplodeAction deserialize(JsonObject object) {
        float radius = GsonHelper.getAsFloat(object, "radius");
        Explosion.BlockInteraction mode = Explosion.BlockInteraction.DESTROY;
        if (object.has("mode")) {
            String modeName = GsonHelper.getAsString(object, "mode").toUpperCase(Locale.ROOT);
            try {
                mode = Explosion.BlockInteraction.valueOf(modeName);
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Invalid explosion mode: " + modeName);
            }
        }
        boolean causesFire = false;
        if (object.has("causesFire")) {
            causesFire = GsonHelper.getAsBoolean(object, "causesFire");
        }
        return new ExplodeAction(radius, causesFire, mode);
    }

    @Override
    public ExplodeAction deserialize(FriendlyByteBuf buffer) {
        return new ExplodeAction(buffer.readFloat(), buffer.readBoolean(), buffer.readEnum(Explosion.BlockInteraction.class));
    }

    public class ExplodeAction implements Action {

        private final float radius;
        private final boolean causesFire;
        private final Explosion.BlockInteraction mode;

        private ExplodeAction(float radius, boolean causesFire, Explosion.BlockInteraction mode) {
            this.radius = radius;
            this.causesFire = causesFire;
            this.mode = mode;
        }

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void accept(GenericBrew brew) {
            Cauldron cauldron = brew.getCauldron();
            Vec3 origin = cauldron.getCenter();
            if (cauldron.getLevel() != null) {
                cauldron.getLevel().explode(null, origin.x, origin.y, origin.z, radius, causesFire, mode);
            }
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("radius", radius);
            object.addProperty("causesFire", causesFire);
            object.addProperty("mode", mode.name());
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeFloat(radius);
            buffer.writeBoolean(causesFire);
            buffer.writeEnum(mode);
        }
    }
}