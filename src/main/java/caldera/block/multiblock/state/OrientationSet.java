package caldera.block.multiblock.state;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Unit;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public interface OrientationSet<O> {

    OrientationSet<Unit> UNIT = new UnitOrientationSet();
    OrientationSet<Direction> HORIZONTAL_FACING = new HorizontalFacingOrientationSet();

    O getInverse(O orientation);

    Vec3i reorient(Vec3i vec, O orientation);

    Vec3 reorient(Vec3 vec, O orientation);

    BlockState getDefaultOrientation(BlockState state);

    O get(BlockState state);

    BlockState set(BlockState state, O orientation);

    void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);

    O getOrientationForPlacement(BlockPlaceContext context);

    class UnitOrientationSet implements OrientationSet<Unit> {

        @Override
        public Unit getInverse(Unit orientation) {
            return Unit.INSTANCE;
        }

        @Override
        public Vec3i reorient(Vec3i vec, Unit orientation) {
            return vec;
        }

        @Override
        public Vec3 reorient(Vec3 vec, Unit orientation) {
            return vec;
        }

        @Override
        public BlockState getDefaultOrientation(BlockState state) {
            return state;
        }

        @Override
        public Unit get(BlockState state) {
            return Unit.INSTANCE;
        }

        @Override
        public BlockState set(BlockState state, Unit orientation) {
            return state;
        }

        @Override
        public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {

        }

        @Override
        public Unit getOrientationForPlacement(BlockPlaceContext context) {
            return Unit.INSTANCE;
        }
    }

    abstract class SinglePropertyOrientationSet<P extends Comparable<P>> implements OrientationSet<P> {

        private final Property<P> orientationProperty;
        private final P defaultOrientation;

        protected SinglePropertyOrientationSet(Property<P> orientationProperty, P defaultOrientation) {
            this.orientationProperty = orientationProperty;
            this.defaultOrientation = validate(defaultOrientation);
        }

        @Override
        public BlockState getDefaultOrientation(BlockState state) {
            return state.setValue(orientationProperty, defaultOrientation);
        }

        @Override
        public P get(BlockState state) {
            return state.getValue(orientationProperty);
        }

        @Override
        public BlockState set(BlockState state, P orientation) {
            return state.setValue(orientationProperty, validate(orientation));
        }

        @Override
        public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(orientationProperty);
        }

        public P validate(P orientation) {
            if (!orientationProperty.getPossibleValues().contains(orientation)) {
                String propertyName = orientationProperty.getName();
                String orientationName = orientationProperty.getName(orientation);
                throw new IllegalArgumentException("Invalid orientation for property %s: %s".formatted(propertyName, orientationName));
            }
            return orientation;
        }
    }

    class HorizontalFacingOrientationSet extends SinglePropertyOrientationSet<Direction> {

        protected HorizontalFacingOrientationSet() {
            super(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        }

        @Override
        public Direction getInverse(Direction facing) {
            validate(facing);
            if (facing.getAxis() == Direction.NORTH.getAxis()) {
                return facing;
            }
            return facing.getOpposite();
        }

        @Override
        public Vec3i reorient(Vec3i vec, Direction orientation) {
            return switch (validate(orientation)) {
                case NORTH -> vec;
                case EAST -> new Vec3i(-vec.getZ(), vec.getY(), vec.getX());
                case SOUTH -> new Vec3i(-vec.getX(), vec.getY(), -vec.getZ());
                case WEST -> new Vec3i(vec.getZ(), vec.getY(), -vec.getX());
                default -> throw new IllegalStateException();
            };
        }

        @Override
        public Vec3 reorient(Vec3 vec, Direction orientation) {
            return switch (validate(orientation)) {
                case NORTH -> vec;
                case EAST -> new Vec3(-vec.z(), vec.y(), vec.x());
                case SOUTH -> new Vec3(-vec.x(), vec.y(), -vec.z());
                case WEST -> new Vec3(vec.z(), vec.y(), -vec.x());
                default -> throw new IllegalStateException();
            };
        }

        @Override
        public Direction getOrientationForPlacement(BlockPlaceContext context) {
            return context.getHorizontalDirection();
        }
    }
}
