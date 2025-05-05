package caldera.block.cauldron.contents;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public interface CauldronContents {

    Codec<CauldronContents> CODEC = Codec.unit(null);

    IItemHandler getItemHandler();

    IFluidHandler getFluidHandler();

    default void remove(CauldronContents newContents) {

    }
}
