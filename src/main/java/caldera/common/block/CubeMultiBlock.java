package caldera.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

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

    public static Vector3i getOffsetFromOrigin(BlockState state) {
        Direction facing = state.getValue(FACING);

        Direction.AxisDirection axisDirectionX = facing.getAxis() == Direction.Axis.X
                ? facing.getAxisDirection()
                : facing.getCounterClockWise().getAxisDirection();
        Direction.AxisDirection axisDirectionZ = facing.getAxis() == Direction.Axis.Z
                ? facing.getAxisDirection()
                : facing.getCounterClockWise().getAxisDirection();

        return new Vector3i(
                axisDirectionX == Direction.AxisDirection.NEGATIVE ? 1 : 0,
                state.getValue(HALF) == DoubleBlockHalf.UPPER ? 1 : 0,
                axisDirectionZ == Direction.AxisDirection.NEGATIVE ? 1 : 0
        );
    }

    public static Direction getFacingFromOffset(Vector3i offset) {
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

    public static DoubleBlockHalf getHalfFromOffset(Vector3i offset) {
        return offset.getY() == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER;
    }

    private static BlockPos getPosForPlacement(BlockItemUseContext blockPlaceContext) {
        Direction facing = blockPlaceContext.getClickedFace();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Vector3d clickOffset = blockPlaceContext.getClickLocation().subtract(
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(HALF);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState currentState, World level, BlockPos replacedPos, BlockState newState, boolean isMoving) {
        if (level.isClientSide() || newState.is(this) || !currentState.is(this)) {
            return;
        }

        replaceWithAir(level, replacedPos.relative(getFacing(currentState, Direction.Axis.X)));
        replaceWithAir(level, replacedPos.relative(getFacing(currentState, Direction.Axis.Y)));
        replaceWithAir(level, replacedPos.relative(getFacing(currentState, Direction.Axis.Z)));

        super.onRemove(newState, level, replacedPos, currentState, isMoving);
    }

    private void replaceWithAir(World level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            Vector3i offset = getOffsetFromOrigin(state);
            if (state.getValue(FACING) == getFacingFromOffset(offset)
                    && state.getValue(HALF) == getHalfFromOffset(offset)
            ) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT);
                level.levelEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getId(state));
            }
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext blockPlaceContext) {
        BlockPos origin = getPosForPlacement(blockPlaceContext);

        if (origin.getY() < 0 || origin.getY() > 254 || streamShape(origin)
                .map(blockPlaceContext.getLevel()::getBlockState)
                .anyMatch(state -> !state.canBeReplaced(blockPlaceContext))
        ) {
            return null;
        }

        Vector3i offset = blockPlaceContext.getClickedPos().subtract(origin);
        return defaultBlockState()
                .setValue(FACING, getFacingFromOffset(offset))
                .setValue(HALF, getHalfFromOffset(offset));
    }

    @Override
    public void setPlacedBy(World level, BlockPos clickedPos, BlockState placedState, LivingEntity entity, ItemStack stack) {
        BlockPos origin = getOrigin(placedState, clickedPos);

        streamShape(origin)
                .filter(pos -> !pos.equals(clickedPos))
                .forEach(pos -> {
                    Vector3i offset = pos.subtract(origin);
                    BlockState stateForPlacement = placedState
                            .setValue(FACING, getFacingFromOffset(offset))
                            .setValue(HALF, getHalfFromOffset(offset));
                    level.setBlock(pos, stateForPlacement, Constants.BlockFlags.DEFAULT);
                });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public long getSeed(BlockState state, BlockPos pos) {
        return MathHelper.getSeed(getOrigin(state, pos));
    }
}
