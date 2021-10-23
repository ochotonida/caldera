package caldera.common.brew.generic.component.trigger.triggers;

import caldera.Caldera;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.trigger.Trigger;
import caldera.common.brew.generic.component.trigger.TriggerType;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class TimerTriggerType extends TriggerType<TimerTriggerType.TimerTrigger> {

    private static final ResourceLocation ID = new ResourceLocation(Caldera.MODID, "timer");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(GenericBrew brew, String identifier) {
        trigger(brew, trigger -> identifier.equals(trigger.identifier()));

    }

    @Override
    public TimerTrigger deserialize(JsonObject object) {
        String identifier = GsonHelper.getAsString(object, "identifier");
        return new TimerTrigger(identifier);
    }

    @Override
    public TimerTrigger deserialize(FriendlyByteBuf buffer) {
        String identifier = buffer.readUtf();
        return new TimerTrigger(identifier);
    }

    public record TimerTrigger(String identifier) implements Trigger {

        @Override
        public ResourceLocation getType() {
            return ID;
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("identifier", identifier);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeUtf(identifier);
        }
    }
}
