package caldera.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class CubeMultiBlock extends Block {

    public static final EnumProperty<DiagonalOrientation> ORIENTATION = EnumProperty.create("orientation", DiagonalOrientation.class);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public CubeMultiBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(ORIENTATION, DiagonalOrientation.NORTH_WEST)
                        .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ORIENTATION);
        builder.add(HALF);
    }

    public BlockPos getRootPosition(BlockState state, BlockPos pos) {
        return pos
                .subtract(state.getValue(ORIENTATION).getOffset())
                .relative(Direction.DOWN, state.getValue(HALF) == DoubleBlockHalf.UPPER ? 1 : 0);
    }

    public Stream<BlockPos> streamBoundingBox(BlockPos root) {
        return BlockPos.betweenClosedStream(root, root.offset(-1, 1, -1));
    }

    private static BlockPos getPosForPlacement(BlockItemUseContext blockPlaceContext) {
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Vector3d clickOffset = blockPlaceContext.getClickLocation().subtract(clickedPos.getX(), clickedPos.getY(), clickedPos.getZ());
        Direction direction = blockPlaceContext.getClickedFace();

        if (direction.getAxis() == Direction.Axis.Y) {
            return clickedPos.offset(clickOffset.x > 0.5 ? 1 : 0, direction == Direction.DOWN ? -1 : 0, clickOffset.z > 0.5 ? 1 : 0);
        } else if (direction.getAxis() == Direction.Axis.X) {
            return clickedPos.offset(direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0, 0, clickOffset.z > 0.5 ? 1 : 0);
        } else {
            return clickedPos.offset(clickOffset.x > 0.5 ? 1 : 0, 0, direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction neighborDirection, BlockState neighborState, IWorld level, BlockPos pos, BlockPos neighborPos) {
        DiagonalOrientation orientation = state.getValue(ORIENTATION);
        DoubleBlockHalf half = state.getValue(HALF);

        if (neighborDirection.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (neighborDirection == Direction.UP)) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) == half || neighborState.getValue(ORIENTATION) != orientation) {
                return Blocks.AIR.defaultBlockState();
            }
        } else if (orientation.getClockWiseDirection() == neighborDirection) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) != half || neighborState.getValue(ORIENTATION) != orientation.getCounterClockWise()) {
                return Blocks.AIR.defaultBlockState();
            }
        } else if (orientation.getCounterClockWiseDirection() == neighborDirection) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) != half || neighborState.getValue(ORIENTATION) != orientation.getClockWise()) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, neighborDirection, neighborState, level, pos, neighborPos);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext blockPlaceContext) {
        BlockPos root = getPosForPlacement(blockPlaceContext);

        if (root.getY() < 0 || root.getY() > 254 || !streamBoundingBox(root)
                .map(blockPlaceContext.getLevel()::getBlockState)
                .allMatch(state -> state.canBeReplaced(blockPlaceContext))) {
            return null;
        }

        Vector3i placementOffset = blockPlaceContext.getClickedPos().subtract(root);
        return defaultBlockState()
                .setValue(ORIENTATION, DiagonalOrientation.fromOffset(placementOffset))
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, placementOffset.getY() == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
    }

    @Override
    public void setPlacedBy(World level, BlockPos clickedPos, BlockState placedState, LivingEntity entity, ItemStack stack) {
        BlockPos root = getRootPosition(placedState, clickedPos);

        streamBoundingBox(root)
                .filter(pos -> !pos.equals(clickedPos))
                .forEach(pos -> {
                    Vector3i offset = root.subtract(pos);
                    BlockState stateForPlacement = placedState
                            .setValue(ORIENTATION, DiagonalOrientation.fromOffset(offset))
                            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, offset.getY() == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
                    level.setBlock(pos, stateForPlacement, Constants.BlockFlags.DEFAULT);
                });
    }

    @Override
    public boolean removedByPlayer(BlockState state, World level, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide && (player.isCreative() || !willHarvest)) {
            if (state.getValue(ORIENTATION) != DiagonalOrientation.defaultOrientation()
                    || state.getValue(HALF) != DoubleBlockHalf.LOWER) {
                BlockPos root = getRootPosition(state, pos);
                BlockState rootState = level.getBlockState(root);
                if (rootState.is(this)
                        && rootState.getValue(ORIENTATION) == DiagonalOrientation.defaultOrientation()
                        && rootState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    level.setBlock(root, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT | Constants.BlockFlags.NO_NEIGHBOR_DROPS);
                    level.levelEvent(player, Constants.WorldEvents.BREAK_BLOCK_EFFECTS, root, Block.getId(rootState));
                }
            }
        }
        return super.removedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void spawnAfterBreak(BlockState p_220062_1_, ServerWorld p_220062_2_, BlockPos p_220062_3_, ItemStack p_220062_4_) {
        super.spawnAfterBreak(p_220062_1_, p_220062_2_, p_220062_3_, p_220062_4_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public long getSeed(BlockState state, BlockPos pos) {
        return MathHelper.getSeed(getRootPosition(state, pos));
    }
}
