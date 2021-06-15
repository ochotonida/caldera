package caldera.common.block;

import caldera.common.init.ModBlockEntityTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class CauldronBlockEntity extends TileEntity implements ITickableTileEntity  {

    public CauldronBlockEntity() {
        super(ModBlockEntityTypes.LARGE_CAULDRON.get());
    }

    public CauldronBlockEntity getController() {
        if (level != null) {
            return LargeCauldronBlock.getController(getBlockState(), getBlockPos(), level);
        }
        return null;
    }

    @Override
    public void tick() {

    }
}
