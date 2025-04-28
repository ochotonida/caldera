package caldera.block.multiblock;

import caldera.block.multiblock.state.MultiblockPart;
import caldera.block.multiblock.state.OrientationSet;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Multiblock<P extends MultiblockPart<P>, O extends Comparable<O>> extends Block {

    private final Map<Vec3i, P> partByRelativePos;
    private final Property<P> multiblockParts;
    private final OrientationSet<O> orientations;
    private final Pair<Vec3i, Vec3i> multiblockBounds;

    protected Multiblock(Properties properties) {
        super(properties);
        this.partByRelativePos = createPartByRelativePos();
        this.multiblockParts = getMultiblockParts();
        this.orientations = getOrientations();
        this.multiblockBounds = createBounds();
        BlockState state = defaultBlockState().setValue(multiblockParts, partByRelativePos.get(Vec3i.ZERO));
        state = orientations.getDefaultOrientation(state);
        registerDefaultState(state);
    }

    private Map<Vec3i, P> createPartByRelativePos() {
        Map<Vec3i, P> result = new HashMap<>(getMultiblockParts().getPossibleValues().size());
        for (P part : getMultiblockParts().getPossibleValues()) {
            if (result.containsKey(part.getRelativePosition())) {
                throw new IllegalArgumentException("Multiple parts located at %s".formatted(part.getRelativePosition()));
            }
            result.put(part.getRelativePosition(), part);
        }
        if (!result.containsKey(Vec3i.ZERO)) {
            throw new IllegalArgumentException("Missing default part");
        }
        return ImmutableMap.copyOf(result);
    }

    private Pair<Vec3i, Vec3i> createBounds() {
        Map<Direction, Integer> result = new HashMap<>(6);
        for (Direction direction : Direction.values()) {
            result.put(direction, 0);
        }
        for (P part : multiblockParts.getPossibleValues()) {
            for (Direction direction : Direction.values()) {
                Vec3i pos = part.getRelativePosition();
                int currentBound = result.get(direction);
                int distance = pos.get(direction.getAxis()) * direction.getAxisDirection().getStep();
                result.put(direction, Math.max(distance, currentBound));
            }
        }
        return Pair.of(
                new Vec3i(result.get(Direction.WEST), result.get(Direction.DOWN), result.get(Direction.NORTH)),
                new Vec3i(result.get(Direction.EAST), result.get(Direction.UP), result.get(Direction.SOUTH))
        );
    }

    protected Pair<Vec3i, Vec3i> getBounds(O orientation) {
        Vec3i minBounds = orientations.reorient(multiblockBounds.getFirst(), orientation);
        Vec3i maxBounds = orientations.reorient(multiblockBounds.getSecond(), orientation);
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (minBounds.get(axis) > maxBounds.get(axis)) {
                int max = minBounds.get(axis);
                int min = maxBounds.get(axis);
                minBounds = minBounds.relative(axis, - min - max);
                maxBounds = maxBounds.relative(axis, - max - min);
            }
        }
        return Pair.of(minBounds, maxBounds);
    }

    protected Vec3 getPlacementOffset() {
        return Vec3.ZERO;
    }

    // createBlockStateDefinition gets called before partProperty is initialized
    protected abstract Property<P> getMultiblockParts();

    // createBlockStateDefinition gets called before orientations is initialized
    protected abstract OrientationSet<O> getOrientations();

    protected Vec3i getOffsetFromOrigin(BlockState state) {
        Vec3i relativePosition = state.getValue(multiblockParts).getRelativePosition();
        return orientations.reorient(relativePosition, orientations.get(state));
    }

    protected BlockState getOriginState(BlockState state) {
        return state.setValue(multiblockParts, partByRelativePos.get(Vec3i.ZERO));
    }

    protected BlockState getStateAtOffset(Vec3i offset, BlockState originState) {
        O orientation = orientations.getInverse(orientations.get(originState));
        Vec3i relativePosition = orientations.reorient(offset, orientation);
        return originState.setValue(multiblockParts, partByRelativePos.get(relativePosition));
    }

    protected BlockState getOriginStateForPlacement(BlockPlaceContext context) {
        return orientations.set(defaultBlockState(), orientations.getOrientationForPlacement(context)) ;
    }

    protected BlockPos getPosForPlacement(BlockPlaceContext context, BlockState originState) {
        O orientation = orientations.get(originState);
        Direction clickedFace = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos();
        Vec3 clickOffset = context.getClickLocation().subtract(
                clickedPos.getX(),
                clickedPos.getY(),
                clickedPos.getZ()
        );

        Pair<Vec3i, Vec3i> bounds = getBounds(orientation);
        boolean p = clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        int clickedFaceOffset = (p ? bounds.getFirst() : bounds.getSecond()).get(clickedFace.getAxis());

        Vec3 placementOffset = orientations.reorient(getPlacementOffset(), orientation)
                .add(clickOffset)
                .with(clickedFace.getAxis(), clickedFaceOffset * (p ? 1 : -1));

        return clickedPos.offset(new Vec3i(
                Mth.floor(placementOffset.x()),
                Mth.floor(placementOffset.y()),
                Mth.floor(placementOffset.z())
        ));
    }

    public Stream<Pair<BlockPos, BlockState>> streamShape(BlockPos pos, BlockState state) {
        BlockPos origin = getOrigin(state, pos);
        O orientation = orientations.get(state);
        return multiblockParts.getAllValues()
                .map(Property.Value::value)
                .map(value -> {
                    Vec3i offset = orientations.reorient(value.getRelativePosition(), orientation);
                    return new Pair<>(
                            origin.offset(offset),
                            state.setValue(multiblockParts, value)
                    );
                });
    }

    public BlockPos getOrigin(BlockState state, BlockPos pos) {
        return pos.subtract(getOffsetFromOrigin(state));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(getMultiblockParts());
        getOrientations().createBlockStateDefinition(builder);
    }

    @Override
    public void onRemove(BlockState currentState, Level level, BlockPos replacedPos, BlockState newState, boolean isMoving) {
        super.onRemove(currentState, level, replacedPos, newState, isMoving);

        if (newState.is(this) || !currentState.is(this)) {
            return;
        }

        streamShape(replacedPos, currentState).forEach(entry -> {
            BlockPos pos = entry.getFirst();
            BlockState state = entry.getSecond();
            if (!pos.equals(replacedPos) && level.getBlockState(pos).equals(state)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            }
        });
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState originState = getOriginStateForPlacement(blockPlaceContext);
        BlockPos origin = getPosForPlacement(blockPlaceContext, originState);

        if (origin.getY() < blockPlaceContext.getLevel().getMinBuildHeight()
                || origin.getY() >= blockPlaceContext.getLevel().getMaxBuildHeight() - 1
                || streamShape(origin, originState)
                .map(Pair::getFirst)
                .map(blockPlaceContext.getLevel()::getBlockState)
                .anyMatch(state -> !state.canBeReplaced(blockPlaceContext))
        ) {
            return null;
        }

        Vec3i offset = blockPlaceContext.getClickedPos().subtract(origin);
        return getStateAtOffset(offset, originState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos clickedPos, BlockState placedState, LivingEntity entity, ItemStack stack) {
        BlockPos origin = getOrigin(placedState, clickedPos);

        streamShape(origin, getOriginState(placedState))
                .filter(entry -> !entry.getFirst().equals(clickedPos))
                .forEach(entry -> level.setBlock(entry.getFirst(), entry.getSecond(), Block.UPDATE_ALL));
    }

    @Override
    @SuppressWarnings("deprecation")
    public long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(getOrigin(state, pos));
    }
}
