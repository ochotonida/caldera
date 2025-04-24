package caldera.registry;

import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;

public class ModSoundTypes {

    public static final SoundType CAULDRON = new DeferredSoundType(
            0.3F,
            0.6F,
            ModSoundEvents.CAULDRON_BREAK,
            ModSoundEvents.CAULDRON_STEP,
            ModSoundEvents.CAULDRON_PLACE,
            ModSoundEvents.CAULDRON_HIT,
            ModSoundEvents.CAULDRON_FALL
    );
}
