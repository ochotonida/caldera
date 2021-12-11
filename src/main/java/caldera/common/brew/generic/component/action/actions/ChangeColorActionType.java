package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.util.ColorHelper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ChangeColorActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<ChangeColorActionType.ChangeColorAction> {

    @Override
    public ChangeColorAction deserialize(JsonObject object) {
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
    public ChangeColorAction deserialize(FriendlyByteBuf buffer) {
        int color = buffer.readInt();
        int transitionTime = buffer.readInt();
        return new ChangeColorAction(color, transitionTime);
    }

    public class ChangeColorAction implements Action {

        private final int color;
        private final int transitionTime;

        private ChangeColorAction(int color, int transitionTime) {
            this.color = color;
            this.transitionTime = transitionTime;
        }

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void accept(GenericBrew brew) {
            brew.getColorInfo().changeColor(color, transitionTime);
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
