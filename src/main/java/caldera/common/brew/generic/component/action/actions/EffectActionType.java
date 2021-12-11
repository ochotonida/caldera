package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.ActionType;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.BiConsumer;

public final class EffectActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<EffectActionType.EffectAction> {

    private final BiConsumer<GenericBrew, String> action;

    public EffectActionType(BiConsumer<GenericBrew, String> action) {
        this.action = action;
    }

    @Override
    public EffectAction deserialize(JsonObject object) {
        String identifier = GsonHelper.getAsString(object, "identifier");
        return new EffectAction(identifier);
    }

    @Override
    public EffectAction deserialize(FriendlyByteBuf buffer) {
        String identifier = buffer.readUtf();
        return new EffectAction(identifier);
    }


    public class EffectAction implements Action {

        private final String identifier;

        private EffectAction(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void accept(GenericBrew brew) {
            action.accept(brew, identifier);
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
