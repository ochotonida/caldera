package caldera.common.recipe.brew.generic.component.trigger.triggers;

import caldera.common.recipe.brew.generic.GenericBrew;
import caldera.common.recipe.brew.generic.component.trigger.Trigger;
import caldera.common.recipe.brew.generic.component.trigger.TriggerType;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class SimpleTriggerType extends TriggerType<SimpleTriggerType.SimpleTrigger> {

    private final ResourceLocation id;

    public SimpleTriggerType(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public void trigger(GenericBrew brew) {
        trigger(brew, trigger -> true);
    }

    @Override
    public SimpleTrigger deserialize(JsonObject object) {
        return new SimpleTrigger();
    }

    @Override
    public SimpleTrigger deserialize(FriendlyByteBuf buffer) {
        return new SimpleTrigger();
    }

    public class SimpleTrigger implements Trigger {

        @Override
        public ResourceLocation getType() {
            return id;
        }

        @Override
        public void serialize(JsonObject object) {

        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {

        }
    }
}
