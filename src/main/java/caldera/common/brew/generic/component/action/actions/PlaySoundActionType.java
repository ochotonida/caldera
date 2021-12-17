package caldera.common.brew.generic.component.action.actions;

import caldera.common.block.cauldron.Cauldron;
import caldera.common.brew.BrewTypeDeserializationContext;
import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.brew.generic.component.action.SimpleAction;
import caldera.common.init.ModActions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class PlaySoundActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<PlaySoundActionType.PlaySoundAction> {

    @Override
    public PlaySoundAction deserialize(JsonObject object, BrewTypeDeserializationContext context) {
        ResourceLocation soundEventId = new ResourceLocation(GsonHelper.getAsString(object, "soundEvent"));
        if (!ForgeRegistries.SOUND_EVENTS.containsKey(soundEventId)) {
            throw new JsonParseException("Unknown sound event: " + soundEventId);
        }
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundEventId);

        float volume = 1;
        float pitch = 1;

        if (object.has("volume")) {
            volume = GsonHelper.getAsFloat(object, "volume");
        }
        if (object.has("pitch")) {
            pitch = GsonHelper.getAsFloat(object, "pitch");
        }

        return new PlaySoundAction(soundEvent, volume, pitch);
    }

    @Nullable
    @Override
    public PlaySoundAction deserialize(FriendlyByteBuf buffer) {
        return null;
    }

    public static PlaySoundAction playSound(SoundEvent soundEvent) {
        return playSound(soundEvent, 1, 1);
    }

    public static PlaySoundAction playSound(SoundEvent soundEvent, float volume, float pitch) {
        return new PlaySoundAction(soundEvent, volume, pitch);
    }

    public static final class PlaySoundAction extends SimpleAction {

        private final SoundEvent soundEvent;
        private final float volume;
        private final float pitch;

        public PlaySoundAction(SoundEvent soundEvent, float volume, float pitch) {
            this.soundEvent = soundEvent;
            this.volume = volume;
            this.pitch = pitch;
        }

        @Override
        public ActionType<?> getType() {
            return ModActions.PLAY_SOUND.get();
        }

        @Override
        public void execute(GenericBrew brew) {
            Cauldron cauldron = brew.getCauldron();
            if (cauldron.getLevel() != null) {
                Vec3 origin = cauldron.getCenter();
                cauldron.getLevel().playSound(null, origin.x, origin.y, origin.z, soundEvent, SoundSource.BLOCKS, volume, pitch);
            }
        }

        @Override
        public void serialize(JsonObject object) {
            // noinspection ConstantConditions
            object.addProperty("soundEvent", soundEvent.getRegistryName().toString());
            object.addProperty("volume", volume);
            object.addProperty("pitch", pitch);
        }

        @Override
        public void serialize(FriendlyByteBuf buffer) { }
    }
}

