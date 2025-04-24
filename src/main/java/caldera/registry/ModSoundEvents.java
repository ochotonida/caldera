package caldera.registry;

import caldera.Caldera;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Caldera.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_BREAK = soundEvent("block.cauldron.break");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_STEP = soundEvent("block.cauldron.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_PLACE = soundEvent("block.cauldron.place");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_HIT = soundEvent("block.cauldron.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> CAULDRON_FALL = soundEvent("block.cauldron.fall");

    private static DeferredHolder<SoundEvent, SoundEvent> soundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(Caldera.id(name)));
    }
}
