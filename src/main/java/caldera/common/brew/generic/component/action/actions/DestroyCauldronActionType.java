package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class DestroyCauldronActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<DestroyCauldronActionType.DestroyCauldronAction> {

    @Override
    public DestroyCauldronAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        boolean shouldDropCauldron = false;
        if (object.has("dropCauldron")) {
            shouldDropCauldron = GsonHelper.getAsBoolean(object, "dropCauldron");
        }
        return new DestroyCauldronAction(shouldDropCauldron);
    }

    public static DestroyCauldronAction destroy(boolean shouldDropCauldron) {
        return new DestroyCauldronAction(shouldDropCauldron);
    }

    public static final class DestroyCauldronAction extends SimpleAction {

        private final boolean shouldDropCauldron;

        public DestroyCauldronAction(boolean shouldDropCauldron) {
            this.shouldDropCauldron = shouldDropCauldron;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.DESTROY_CAULDRON.get();
        }

        @Override
        public void accept(GenericBrew brew) {
            brew.getCauldron().destroy(shouldDropCauldron);
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("dropCauldron", shouldDropCauldron);
        }
    }
}
