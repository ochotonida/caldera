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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ORIENTATION);
        builder.add(HALF);
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
        BlockPos posForPlacement = getPosForPlacement(blockPlaceContext);

        if (posForPlacement.getY() < 0 || posForPlacement.getY() > 254
                || !blockPlaceContext.getLevel()
                .getBlockStates(new AxisAlignedBB(posForPlacement, posForPlacement.offset(-1, 1, -1)))
                .allMatch(state -> state.canBeReplaced(blockPlaceContext))) {
            return null;
        }

        Vector3i placementOffset = blockPlaceContext.getClickedPos().subtract(posForPlacement);
        return defaultBlockState()
                .setValue(ORIENTATION, DiagonalOrientation.fromOffset(placementOffset))
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, placementOffset.getY() == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
    }

    public void setPlacedBy(World level, BlockPos clickedPos, BlockState state, LivingEntity entity, ItemStack stack) {
        BlockPos posForPlacement = clickedPos
                .offset(state.getValue(ORIENTATION).getOffset())
                .relative(Direction.DOWN, state.getValue(HALF) == DoubleBlockHalf.UPPER ? 1 : 0);

        BlockPos.betweenClosedStream(posForPlacement, posForPlacement.offset(-1, 1, -1))
                .filter(pos -> !pos.equals(clickedPos))
                .forEach(pos -> {
                    BlockPos offset = posForPlacement.subtract(pos);
                    BlockState stateForPlacement = state
                            .setValue(ORIENTATION, DiagonalOrientation.fromOffset(offset))
                            .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, offset.getY() == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
                    level.setBlock(pos, stateForPlacement, Constants.BlockFlags.DEFAULT);
                });
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public long getSeed(BlockState state, BlockPos pos) {
        pos = pos.subtract(state.getValue(ORIENTATION).getOffset());
        if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
        }
        return MathHelper.getSeed(pos);
    }
}
