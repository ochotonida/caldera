package caldera.common.block;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.vector.Vector3i;

public enum DiagonalOrientation implements IStringSerializable {
    NORTH_WEST("north_west", Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.NEGATIVE),
    NORTH_EAST("north_east", Direction.AxisDirection.POSITIVE, Direction.AxisDirection.NEGATIVE),
    SOUTH_EAST("south_east", Direction.AxisDirection.POSITIVE, Direction.AxisDirection.POSITIVE),
    SOUTH_WEST("south_west", Direction.AxisDirection.NEGATIVE, Direction.AxisDirection.POSITIVE);

    private final String name;
    private final Vector3i offset;

    DiagonalOrientation(String name, Direction.AxisDirection axisDirectionX, Direction.AxisDirection axisDirectionZ) {
        this.name = name;
        this.offset = new Vector3i(axisDirectionX == Direction.AxisDirection.POSITIVE ? -1 : 0, 0, axisDirectionZ == Direction.AxisDirection.POSITIVE ? -1 : 0);
    }

    public static DiagonalOrientation defaultOrientation() {
        return NORTH_WEST;
    }

    public static DiagonalOrientation fromDirections(Direction.AxisDirection axisDirectionX, Direction.AxisDirection axisDirectionZ) {
        if (axisDirectionX == Direction.AxisDirection.NEGATIVE) {
            return axisDirectionZ == Direction.AxisDirection.NEGATIVE ? NORTH_WEST : SOUTH_WEST;
        } else {
            return axisDirectionZ == Direction.AxisDirection.NEGATIVE ? NORTH_EAST : SOUTH_EAST;
        }
    }

    public static DiagonalOrientation fromOffset(Vector3i offset) {
        return fromDirections(offset.getX() == 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE, offset.getZ() == 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
    }

    public Vector3i getOffset() {
        return offset;
    }

    public DiagonalOrientation getClockWise() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    public DiagonalOrientation getCounterClockWise() {
        return values()[(this.ordinal() + values().length - 1) % values().length];
    }

    public Direction getClockWiseDirection() {
        switch (this) {
            case NORTH_EAST:
                return Direction.EAST;
            case SOUTH_EAST:
                return Direction.SOUTH;
            case SOUTH_WEST:
                return Direction.WEST;
            default:
                return Direction.NORTH;
        }
    }

    public Direction getCounterClockWiseDirection() {
        return getCounterClockWise().getClockWiseDirection();
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
