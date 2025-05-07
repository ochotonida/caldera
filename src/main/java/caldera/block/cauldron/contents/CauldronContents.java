package caldera.block.cauldron.contents;

import caldera.registry.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public interface CauldronContents {

    Codec<CauldronContents> CODEC = ModRegistries.CAULDRON_CONTENT_TYPES.byNameCodec()
            .dispatch(CauldronContents::getType, Type::codec);

    Type<?> getType();

    IItemHandler getItemHandler();

    IFluidHandler getFluidHandler();

    record Type<T extends CauldronContents>(MapCodec<T> codec) {

    }
}
