package caldera.common.init;

import net.minecraft.block.SoundType;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.util.ForgeSoundType;

public class ModSoundTypes {

    public static final SoundType CAULDRON = new ForgeSoundType(0.3F, 0.6F,
            () -> SoundEvents.LANTERN_BREAK,
            () -> SoundEvents.LANTERN_STEP,
            () -> SoundEvents.ANVIL_PLACE,
            () -> SoundEvents.LANTERN_HIT,
            () -> SoundEvents.LANTERN_FALL
    );
}
