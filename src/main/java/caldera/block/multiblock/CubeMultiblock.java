package caldera.block.multiblock;

import caldera.block.multiblock.state.Octant;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public abstract class CubeMultiblock<O extends Comparable<O>> extends Multiblock<Octant, O> {

    public static final EnumProperty<Octant> OCTANT = EnumProperty.create("octant", Octant.class);

    public CubeMultiblock(Properties properties) {
        super(properties);
    }

    @Override
    protected Property<Octant> getMultiblockParts() {
        return OCTANT;
    }

    @Override
    protected Vec3 getPlacementOffset() {
        return new Vec3(-0.5, 0, -0.5);
    }
}
