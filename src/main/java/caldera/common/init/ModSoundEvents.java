package caldera.common.init;

import caldera.Caldera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSoundEvents {

    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Caldera.MODID);

    public static final RegistryObject<SoundEvent> CAULDRON_BREAK = soundEvent("block.cauldron.break");
    public static final RegistryObject<SoundEvent> CAULDRON_STEP = soundEvent("block.cauldron.step");
    public static final RegistryObject<SoundEvent> CAULDRON_PLACE = soundEvent("block.cauldron.place");
    public static final RegistryObject<SoundEvent> CAULDRON_HIT = soundEvent("block.cauldron.hit");
    public static final RegistryObject<SoundEvent> CAULDRON_FALL = soundEvent("block.cauldron.fall");

    public static final RegistryObject<SoundEvent> CAULDRON_RETURN_INERT_INGREDIENT = soundEvent("block.cauldron.return_inert_ingredient");

    private static RegistryObject<SoundEvent> soundEvent(String name) {
        return REGISTRY.register(name, () -> new SoundEvent(new ResourceLocation(Caldera.MODID, name)));
    }
}
