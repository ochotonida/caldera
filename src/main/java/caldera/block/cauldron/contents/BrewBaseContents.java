package caldera.block.cauldron.contents;

import caldera.registry.ModCauldronContentTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.EmptyFluidHandler;

public class BrewBaseContents extends CauldronContentsWithInventory {

    public static final MapCodec<BrewBaseContents> CODEC = CauldronItemInventory.CODEC.fieldOf("items")
            .xmap(BrewBaseContents::new, BrewBaseContents::getItemHandler);

    public BrewBaseContents() {
        super(NonNullList.create());
    }

    public BrewBaseContents(CauldronItemInventory itemInventory) {
        super(itemInventory);
    }

    @Override
    public Type<?> getType() {
        return ModCauldronContentTypes.BREW_BASE.get();
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return EmptyFluidHandler.INSTANCE;
    }
}
