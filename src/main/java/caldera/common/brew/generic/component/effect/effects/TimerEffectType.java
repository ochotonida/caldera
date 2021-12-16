package caldera.common.brew.generic.component.effect.effects;

import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.effect.Effect;
import caldera.common.brew.generic.component.effect.EffectProvider;
import caldera.common.brew.generic.component.effect.EffectProviderType;
import caldera.common.init.ModEffectProviders;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class TimerEffectType extends ForgeRegistryEntry<EffectProviderType<?>> implements EffectProviderType<TimerEffectType.TimerEffectProvider> {

    @Override
    public TimerEffectProvider deserialize(JsonObject object, BrewTypeDeserializationContext context) {
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

    public static TimerEffectProvider timer(int duration) {
        return new TimerEffectProvider(duration);
    }

    public static final class TimerEffectProvider extends EffectProvider {

        private final int duration;

        public TimerEffectProvider(int duration) {
            this.duration = duration;
        }

        @Override
        public EffectProviderType<?> getType() {
            return ModEffectProviders.TIMER.get();
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
        public Effect create(GenericBrew brew) {
            return new TimerEffect(brew, duration);
        }

        @Override
        public Effect loadEffect(GenericBrew brew, CompoundTag tag) {
            int time = tag.getInt("TimeRemaining");
            return new TimerEffect(brew, time);
        }

        public class TimerEffect implements Effect {

            private final GenericBrew brew;
            private int timeRemaining;

            public TimerEffect(GenericBrew brew, int timeRemaining) {
                this.brew = brew;
                this.timeRemaining = timeRemaining;
            }

            @Override
            public void tick() {
                if (brew.getCauldron().getLevel() != null && brew.getCauldron().getLevel().isClientSide()) {
                    return;
                }
                if (--timeRemaining <= 0) {
                    brew.endEffect(getIdentifier());
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
}
