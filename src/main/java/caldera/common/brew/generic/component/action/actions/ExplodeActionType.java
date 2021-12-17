package caldera.common.brew.generic.component.action.actions;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import caldera.common.util.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class ExplodeActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<ExplodeActionType.ExplodeAction> {

    @Override
    public ExplodeAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        float radius = GsonHelper.getAsFloat(object, "radius");
        Explosion.BlockInteraction mode = Explosion.BlockInteraction.DESTROY;
        if (object.has("mode")) {
            mode = JsonHelper.getAsEnumValue(object, "mode", Explosion.BlockInteraction.class);
        }
        boolean causesFire = false;
        if (object.has("causesFire")) {
            causesFire = GsonHelper.getAsBoolean(object, "causesFire");
        }
        return new ExplodeAction(radius, causesFire, mode);
    }

    @Nullable
    @Override
    public ExplodeAction deserialize(FriendlyByteBuf buffer) {
        return null;
    }

    public static ExplodeAction explode(float radius) {
        return explode(radius, false, Explosion.BlockInteraction.DESTROY);
    }

    public static ExplodeAction explode(float radius, boolean causesFire, Explosion.BlockInteraction mode) {
        return new ExplodeAction(radius, causesFire, mode);
    }

    public static final class ExplodeAction extends SimpleAction {

        private final float radius;
        private final boolean causesFire;
        private final Explosion.BlockInteraction mode;

        public ExplodeAction(float radius, boolean causesFire, Explosion.BlockInteraction mode) {
            this.radius = radius;
            this.causesFire = causesFire;
            this.mode = mode;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.EXPLODE.get();
        }

        @Override
        public void execute(GenericBrew brew) {
            Cauldron cauldron = brew.getCauldron();
            Vec3 origin = cauldron.getCenter();
            // noinspection ConstantConditions
            cauldron.getLevel().explode(null, origin.x, origin.y, origin.z, radius, causesFire, mode);
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("radius", radius);
            object.addProperty("causesFire", causesFire);
            object.add("mode", JsonHelper.writeEnumValue(mode));
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) { }
    }
}
