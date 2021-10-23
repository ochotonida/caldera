package caldera.common.brew.generic.component.effect;

import caldera.common.brew.generic.GenericBrew;
import caldera.common.brew.generic.component.GenericBrewTypeComponent;
import net.minecraft.nbt.CompoundTag;

public interface EffectProvider extends GenericBrewTypeComponent.Instance {

    Effect create(GenericBrew brew);

    Effect loadEffect(GenericBrew brew, CompoundTag tag);

}
