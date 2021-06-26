package caldera.common.init;

import net.minecraft.block.SoundType;
import net.minecraftforge.common.util.ForgeSoundType;

public class ModSoundTypes {

    public static final SoundType CAULDRON = new ForgeSoundType(
            0.3F,
            0.6F,
            ModSoundEvents.CAULDRON_BREAK,
            ModSoundEvents.CAULDRON_STEP,
            ModSoundEvents.CAULDRON_PLACE,
            ModSoundEvents.CAULDRON_HIT,
            ModSoundEvents.CAULDRON_FALL
    );
}
