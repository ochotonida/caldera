package caldera.block.multiblock.state;

import net.minecraft.core.Vec3i;

public interface MultiblockPart<P> extends Comparable<P> {

    Vec3i getRelativePosition();
}
