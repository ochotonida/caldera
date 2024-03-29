package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ChangeColorActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<ChangeColorActionType.ChangeColorAction> {

    @Override
    public ChangeColorAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        int color = ColorHelper.readColor(object, "color");
        int transitionTime = 0;
        if (object.has("transitionTime")) {
            transitionTime = GsonHelper.getAsInt(object, "transitionTime");
            if (transitionTime < 0) {
                throw new JsonParseException("Transition time must be 0 or positive");
            }
        }
        return new ChangeColorAction(color, transitionTime);
    }

    @Override
    public boolean shouldSendToClients() {
        return true;
    }

    @Override
    public ChangeColorAction deserialize(FriendlyByteBuf buffer) {
        int color = buffer.readInt();
        int transitionTime = buffer.readInt();
        return new ChangeColorAction(color, transitionTime);
    }

    public static ChangeColorAction setColor(int color) {
        return changeColor(color, 0);
    }

    public static ChangeColorAction changeColor(int color, int transitionTime) {
        return new ChangeColorAction(color, transitionTime);
    }

    public static final class ChangeColorAction extends SimpleAction {

        private final int color;
        private final int transitionTime;

        public ChangeColorAction(int color, int transitionTime) {
            this.color = color;
            this.transitionTime = transitionTime;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.CHANGE_COLOR.get();
        }

        @Override
        public void execute(GenericBrew brew) {
            brew.changeColor(color, transitionTime);
        }

        @Override
        public void serialize(JsonObject object) {
            object.add("color", ColorHelper.writeColor(color));
            object.addProperty("transitionTime", transitionTime);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeInt(color);
            buffer.writeInt(transitionTime);
        }
    }
}
