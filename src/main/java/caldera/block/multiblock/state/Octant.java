package caldera.block.multiblock.state;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;

public enum Octant implements StringRepresentable, MultiblockPart<Octant> {
    LOWER_NORTHWEST("lower_northwest", Direction.WEST, Direction.DOWN, Direction.NORTH, Direction.NORTH, new Vec3i(0, 0, 0)),
    LOWER_NORTHEAST("lower_northeast", Direction.EAST, Direction.DOWN, Direction.NORTH, Direction.EAST,  new Vec3i(1, 0, 0)),
    LOWER_SOUTHWEST("lower_southwest", Direction.WEST, Direction.DOWN, Direction.SOUTH, Direction.WEST,  new Vec3i(0, 0, 1)),
    LOWER_SOUTHEAST("lower_southeast", Direction.EAST, Direction.DOWN, Direction.SOUTH, Direction.SOUTH, new Vec3i(1, 0, 1)),
    UPPER_NORTHWEST("upper_northwest", Direction.WEST, Direction.UP,   Direction.NORTH, Direction.NORTH, new Vec3i(0, 1, 0)),
    UPPER_NORTHEAST("upper_northeast", Direction.EAST, Direction.UP,   Direction.NORTH, Direction.EAST,  new Vec3i(1, 1, 0)),
    UPPER_SOUTHWEST("upper_southwest", Direction.WEST, Direction.UP,   Direction.SOUTH, Direction.WEST,  new Vec3i(0, 1, 1)),
    UPPER_SOUTHEAST("upper_southeast", Direction.EAST, Direction.UP,   Direction.SOUTH, Direction.SOUTH, new Vec3i(1, 1, 1));

    private final String name;
    private final Direction localX;
    private final Direction localY;
    private final Direction localZ;
    private final Direction primaryFacing;
    private final Vec3i relativePosition;

    Octant(String name, Direction localX, Direction localY, Direction localZ, Direction primaryFacing, Vec3i relativePosition) {
        this.name = name;
        this.localX = localX;
        this.localY = localY;
        this.localZ = localZ;
        this.primaryFacing = primaryFacing;
        this.relativePosition = relativePosition;
    }

    public Direction getLocalX() {
        return localX;
    }

    public Direction getLocalY() {
        return localY;
    }

    public Direction getLocalZ() {
        return localZ;
    }

    public boolean isLower() {
        return getLocalY() == Direction.DOWN;
    }

    public Direction getPrimaryFacing() {
        return primaryFacing;
    }

    public Vec3i getRelativePosition() {
        return relativePosition;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getSerializedName() {
        return toString();
    }
}
