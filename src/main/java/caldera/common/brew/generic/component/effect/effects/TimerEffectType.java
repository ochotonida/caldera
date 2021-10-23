package caldera.common.brew.generic.component.effect.effects;

import caldera.Caldera;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.trigger.Triggers;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TimerEffectType implements EffectProviderType<TimerEffectType.TimerEffectProvider> {

    public static final ResourceLocation ID = new ResourceLocation(Caldera.MODID, "timer");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TimerEffectProvider deserialize(JsonObject object) {
        return null;
    }

    @Override
    public TimerEffectProvider deserialize(FriendlyByteBuf buffer) {
        return null;
    }

    public static record TimerEffectProvider(String identifier, int time) implements EffectProvider {

        @Override
        public ResourceLocation getType() {
            return ID;
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("identifier", identifier);
            object.addProperty("time", time);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeUtf(identifier);
            buffer.writeInt(time);
        }

        @Override
        public Effect create(GenericBrew brew) {
            return new TimerEffect(brew, identifier, time);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag) {
            String identifier = tag.getString("Identifier");
            int time = tag.getInt("Time");
            return new TimerEffect(brew, identifier, time);
        }
    }

    public static class TimerEffect implements Effect {

        private final GenericBrew brew;
        private final String identifier;
        private int time;

        public TimerEffect(GenericBrew brew, String identifier, int time) {
            this.brew = brew;
            this.identifier = identifier;
            this.time = time;
        }

        @Override
        public void tick() {
            if (--time <= 0) {
                brew.removeEffect(identifier);
                Triggers.TIMER.trigger(brew, identifier);
            }
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt("Time", time);
        }
    }
}
