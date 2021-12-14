package caldera.common.brew.generic.component.effect.effects;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.brew.generic.component.effect.EffectProviders;
import caldera.common.brew.generic.component.trigger.Triggers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TimerEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<TimerEffectType.TimerEffectProvider> {

    @Override
    public TimerEffectProvider deserialize(JsonObject object) {
        int time = GsonHelper.getAsInt(object, "duration");
        if (time < 10) {
            throw new JsonParseException("Timer duration must be greater than or equal to 10 ticks");
        }
        return new TimerEffectProvider(time);
    }

    @Override
    public TimerEffectProvider deserialize(FriendlyByteBuf buffer) {
        return new TimerEffectProvider(buffer.readInt());
    }

    public TimerEffectProvider timer(int duration) {
        return new TimerEffectProvider(duration);
    }

    public record TimerEffectProvider(int duration) implements EffectProvider {

        @Override
        public EffectProviderType<?> getType() {
            return EffectProviders.TIMER.get();
        }

        @Override
        public void serialize(JsonObject object) {
            object.addProperty("duration", duration);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) {
            buffer.writeInt(duration);
        }

        @Override
        public Effect create(GenericBrew brew, String identifier) {
            return new TimerEffect(brew, identifier, duration);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag, String identifier) {
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
            } else {
                brew.getCauldron().setChanged();
            }
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt("TimeRemaining", timeRemaining);
        }
    }
}
