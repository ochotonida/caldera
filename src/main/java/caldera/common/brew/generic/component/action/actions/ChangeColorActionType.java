package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ChangeColorActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<ChangeColorActionType.ChangeColorAction> {

    @Override
    public ChangeColorAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        int baseColor = ColorHelper.readColor(object, "base_color");
        int overlayColor = ColorHelper.readColor(object, "overlay_color");
        int transitionTime = 0;
        if (object.has("transition_time")) {
            transitionTime = GsonHelper.getAsInt(object, "transition_time");
            if (transitionTime < 0) {
                throw new JsonParseException("Transition time must be 0 or positive");
            }
        }
        return new ChangeColorAction(baseColor, overlayColor, transitionTime);
    }

    public static ChangeColorAction setColor(int baseColor, int overlayColor) {
        return changeColor(baseColor, overlayColor, 0);
    }

    public static ChangeColorAction changeColor(int baseColor, int overlayColor, int transitionTime) {
        return new ChangeColorAction(baseColor, overlayColor, transitionTime);
    }

    public static final class ChangeColorAction extends SimpleAction {

        private final int baseColor;
        private final int overlayColor;
        private final int transitionTime;

        public ChangeColorAction(int baseColor, int overlayColor, int transitionTime) {
            this.baseColor = baseColor;
            this.overlayColor = overlayColor;
            this.transitionTime = transitionTime;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.CHANGE_COLOR.get();
        }

        @Override
        public void accept(GenericBrew brew) {
            brew.changeColor(baseColor, overlayColor, transitionTime);
            brew.sendColorUpdate();
        }

        @Override
        public void serialize(JsonObject object) {
            object.add("base_color", ColorHelper.writeColor(baseColor));
            object.add("overlay_color", ColorHelper.writeColor(overlayColor));
            object.addProperty("transition_time", transitionTime);
        }
    }
}
