package caldera.common.brew.generic.component.effect.effects;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.trigger.Triggers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TimerEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<TimerEffectType.TimerEffectProvider> {

    @Override
    public TimerEffectProvider deserialize(JsonObject object, String identifier) {
        int time = GsonHelper.getAsInt(object, "duration");
        if (time < 1) {
            throw new JsonParseException("Timer duration must be greater than 0");
        }
        return new TimerEffectProvider(identifier, time);
    }

    @Override
    public TimerEffectProvider deserialize(FriendlyByteBuf buffer, String identifier) {
        return new TimerEffectProvider(identifier, buffer.readInt());
    }

    public class TimerEffectProvider implements EffectProvider {

        private final String identifier;
        private final int time;

        public TimerEffectProvider(String identifier, int time) {
            this.identifier = identifier;
            this.time = time;
        }

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("duration", time);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeInt(time);
        }

        @Override
        public Effect create(GenericBrew brew) {
            return new TimerEffect(brew, identifier, time * 20);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag) {
            int time = tag.getInt("TimeRemaining");
            return new TimerEffect(brew, identifier, time);
        }
    }

    public static class TimerEffect implements Effect {

        private final GenericBrew brew;
        private final String identifier;
        private int timeRemaining;

        public TimerEffect(GenericBrew brew, String identifier, int timeRemaining) {
            this.brew = brew;
            this.identifier = identifier;
            this.timeRemaining = timeRemaining;
        }

        @Override
        public void tick() {
            if (--timeRemaining <= 0) {
                brew.removeEffect(identifier);
                Triggers.TIMER.get().trigger(brew, identifier);
            }
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt("TimeRemaining", timeRemaining);
        }
    }
}
