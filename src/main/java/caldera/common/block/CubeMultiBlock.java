package caldera.common.block;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class CubeMultiBlock extends Block {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public CubeMultiBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(FACING, Direction.SOUTH)
                        .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    public static Direction getFacing(BlockState state, Direction.Axis axis) {
        if (axis == Direction.Axis.Y) {
            return state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        } else {
            Direction facing = state.getValue(FACING);
            if (axis == Direction.Axis.X) {
                return facing.getAxis() == Direction.Axis.X ? facing : facing.getCounterClockWise();
            } else {
                return facing.getAxis() == Direction.Axis.Z ? facing : facing.getCounterClockWise();
            }
        }
    }

    public static BlockPos getOrigin(BlockState state, BlockPos pos) {
        return pos.subtract(getOffsetFromOrigin(state));
    }

    public static Stream<BlockPos> streamShape(BlockPos origin) {
        return BlockPos.betweenClosedStream(origin, origin.offset(1, 1, 1));
    }

    public static Vec3i getOffsetFromOrigin(BlockState state) {
        Direction facing = state.getValue(FACING);

        Direction.AxisDirection axisDirectionX = facing.getAxis() == Direction.Axis.X
                ? facing.getAxisDirection()
                : facing.getCounterClockWise().getAxisDirection();
        Direction.AxisDirection axisDirectionZ = facing.getAxis() == Direction.Axis.Z
                ? facing.getAxisDirection()
                : facing.getCounterClockWise().getAxisDirection();

        return new Vec3i(
                axisDirectionX == Direction.AxisDirection.NEGATIVE ? 1 : 0,
                state.getValue(HALF) == DoubleBlockHalf.UPPER ? 1 : 0,
                axisDirectionZ == Direction.AxisDirection.NEGATIVE ? 1 : 0
        );
    }

    public static Direction getFacingFromOffset(Vec3i offset) {
        Direction.AxisDirection axisDirectionX = offset.getX() == 0
                ? Direction.AxisDirection.POSITIVE
                : Direction.AxisDirection.NEGATIVE;
        Direction.AxisDirection axisDirectionZ = offset.getZ() == 0
                ? Direction.AxisDirection.POSITIVE
                : Direction.AxisDirection.NEGATIVE;

        if (axisDirectionX == Direction.AxisDirection.POSITIVE) {
            return axisDirectionZ == Direction.AxisDirection.POSITIVE ? Direction.SOUTH : Direction.EAST;
        } else {
            return axisDirectionZ == Direction.AxisDirection.POSITIVE ? Direction.WEST : Direction.NORTH;
        }
    }

    public static DoubleBlockHalf getHalfFromOffset(Vec3i offset) {
        return offset.getY() == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
    }

    private static BlockPos getPosForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction facing = blockPlaceContext.getClickedFace();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Vec3 clickOffset = blockPlaceContext.getClickLocation().subtract(
                clickedPos.getX(),
                clickedPos.getY(),
                clickedPos.getZ()
        );

        int yOffset = facing == Direction.DOWN ? -1 : 0;
        int xOffset;
        int zOffset;

        if (facing.getAxis() == Direction.Axis.X) {
            xOffset = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -1 : 0;
        } else {
            xOffset = clickOffset.x < 0.5 ? -1 : 0;
        }

        if (facing.getAxis() == Direction.Axis.Z) {
            zOffset = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -1 : 0;
        } else {
            zOffset = clickOffset.z < 0.5 ? -1 : 0;
        }

        return clickedPos.offset(xOffset, yOffset, zOffset);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(HALF);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState currentState, Level level, BlockPos replacedPos, BlockState newState, boolean isMoving) {
        super.onRemove(currentState, level, replacedPos, newState, isMoving);

        if (level.isClientSide() || newState.is(this) || !currentState.is(this)) {
            return;
        }

        replaceWithAir(level, replacedPos.relative(getFacing(currentState, Direction.Axis.X)));
        replaceWithAir(level, replacedPos.relative(getFacing(currentState, Direction.Axis.Y)));
        replaceWithAir(level, replacedPos.relative(getFacing(currentState, Direction.Axis.Z)));
    }

    private void replaceWithAir(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            Vec3i offset = getOffsetFromOrigin(state);
            if (state.getValue(FACING) == getFacingFromOffset(offset)
                    && state.getValue(HALF) == getHalfFromOffset(offset)
            ) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos origin = getPosForPlacement(blockPlaceContext);

        if (origin.getY() < 0 || origin.getY() > 254 || streamShape(origin)
                .map(blockPlaceContext.getLevel()::getBlockState)
                .anyMatch(state -> !state.canBeReplaced(blockPlaceContext))
        ) {
            return null;
        }

        Vec3i offset = blockPlaceContext.getClickedPos().subtract(origin);
        return defaultBlockState()
                .setValue(FACING, getFacingFromOffset(offset))
                .setValue(HALF, getHalfFromOffset(offset));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos clickedPos, BlockState placedState, LivingEntity entity, ItemStack stack) {
        BlockPos origin = getOrigin(placedState, clickedPos);

        streamShape(origin)
                .filter(pos -> !pos.equals(clickedPos))
                .forEach(pos -> {
                    Vec3i offset = pos.subtract(origin);
                    BlockState stateForPlacement = placedState
                            .setValue(FACING, getFacingFromOffset(offset))
                            .setValue(HALF, getHalfFromOffset(offset));
                    level.setBlock(pos, stateForPlacement, Block.UPDATE_ALL);
                });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public long getSeed(BlockState state, BlockPos pos) {
        return Mth.getSeed(getOrigin(state, pos));
    }
}
