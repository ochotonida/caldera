package caldera.common.brew.generic.component.action.actions;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.action.Action;
import caldera.common.brew.generic.component.action.ActionType;
import caldera.common.recipe.Cauldron;
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

public class PlaySoundActionType extends ForgeRegistryEntry<ActionType<?>> implements ActionType<PlaySoundActionType.PlaySoundAction> {

    @Override
    public PlaySoundAction deserialize(JsonObject object) {
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

    @Override
    public PlaySoundAction deserialize(FriendlyByteBuf buffer) {
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(buffer.readResourceLocation());
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        return new PlaySoundAction(soundEvent, volume, pitch);
    }

    public class PlaySoundAction implements Action {

        private final SoundEvent soundEvent;
        private final float volume;
        private final float pitch;

        private PlaySoundAction(SoundEvent soundEvent, float volume, float pitch) {
            this.soundEvent = soundEvent;
            this.volume = volume;
            this.pitch = pitch;
        }

        @Override
        public ResourceLocation getType() {
            return getRegistryName();
        }

        @Override
        public void accept(GenericBrew brew) {
            Cauldron cauldron = brew.getCauldron();
            Vec3 origin = cauldron.getCenter();
            if (cauldron.getLevel() != null && !cauldron.getLevel().isClientSide()) { // TODO do this on clients only
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
        public void serialize(FriendlyByteBuf buffer) {
            //noinspection ConstantConditions
            buffer.writeResourceLocation(soundEvent.getRegistryName());
            buffer.writeFloat(volume);
            buffer.writeFloat(pitch);
        }
    }
}

