package caldera.common;

import caldera.common.util.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.HashMap;
import java.util.Map;

public class CauldronBlock extends Block {

    private static final Map<Direction, VoxelShape> LOWER_SHAPES = new HashMap<>();
    private static final Map<Direction, VoxelShape> UPPER_SHAPES = new HashMap<>();

    static {
        VoxelShape lowerShape = VoxelShapes.join(
                VoxelShapes.or(
                        Block.box(0, 2, 0, 16, 16, 16),
                        Block.box(0, 0, 0, 15, 2, 15)
                ),
                Block.box(0, 2, 0, 14, 16, 14),
                IBooleanFunction.ONLY_FIRST
        );

        VoxelShape upperShape = VoxelShapes.join(
                Block.box(0, 0, 0, 16, 8, 16),
                VoxelShapes.or(
                        Block.box(0, 0, 0, 14, 8, 14),
                        Block.box(15, 2, 0, 16, 5, 16),
                        Block.box(0, 2, 15, 16, 5, 16)
                ),
                IBooleanFunction.ONLY_FIRST
        );

        Direction.Plane.HORIZONTAL.forEach(direction -> {

            LOWER_SHAPES.put(direction, VoxelShapeHelper.rotateShape(lowerShape, direction));
            UPPER_SHAPES.put(direction, VoxelShapeHelper.rotateShape(upperShape, direction));
        });
    }

    public CauldronBlock(Properties properties) {
        super(properties);
        registerDefaultState(
                defaultBlockState()
                        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF);
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext selectionContext) {
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPES.get(direction) : UPPER_SHAPES.get(direction);
    }
}
