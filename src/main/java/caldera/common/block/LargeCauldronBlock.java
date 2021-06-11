package caldera.common.block;

import caldera.common.util.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.HashMap;
import java.util.Map;

public class LargeCauldronBlock extends CubeMultiBlock {

    private static final Map<DiagonalOrientation, VoxelShape> LOWER_SHAPES = new HashMap<>();
    private static final Map<DiagonalOrientation, VoxelShape> UPPER_SHAPES = new HashMap<>();

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

        for (DiagonalOrientation orientation : DiagonalOrientation.values()) {
            LOWER_SHAPES.put(orientation, VoxelShapeHelper.rotateShape(lowerShape, orientation.getClockWiseDirection()));
            UPPER_SHAPES.put(orientation, VoxelShapeHelper.rotateShape(upperShape, orientation.getClockWiseDirection()));
        }
    }

    public LargeCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext selectionContext) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_SHAPES.get(state.getValue(ORIENTATION));
        } else {
            return UPPER_SHAPES.get(state.getValue(ORIENTATION));
        }
    }
}
