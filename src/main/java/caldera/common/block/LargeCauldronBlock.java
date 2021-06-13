package caldera.common.block;

import caldera.common.util.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

public class LargeCauldronBlock extends CubeMultiBlock {

    private static final Map<Direction, VoxelShape> LOWER_SHAPES = new HashMap<>();
    private static final Map<Direction, VoxelShape> UPPER_SHAPES = new HashMap<>();

    static {
        VoxelShape lowerShape = VoxelShapes.join(
                // start with a slab and a slightly wider cube on top
                VoxelShapes.or(
                        Block.box(0, 2, 0, 16, 16, 16),
                        Block.box(0, 0, 0, 15, 2, 15)
                ),
                // subtract the basin from the first two shapes
                Block.box(0, 4, 0, 14, 16, 14),
                IBooleanFunction.ONLY_FIRST
        );

        VoxelShape upperShape = VoxelShapes.join(
                // start with a slab
                Block.box(0, 0, 0, 16, 8, 16),
                // subtract the horizontal slits and the basin from the first shape
                VoxelShapes.or(
                        Block.box(0, 0, 0, 14, 8, 14),
                        Block.box(15, 2, 0, 16, 5, 16),
                        Block.box(0, 2, 15, 16, 5, 16)
                ),
                IBooleanFunction.ONLY_FIRST
        );

        Direction.Plane.HORIZONTAL.forEach(facing -> {
            LOWER_SHAPES.put(facing, VoxelShapeHelper.rotateShape(lowerShape, facing));
            UPPER_SHAPES.put(facing, VoxelShapeHelper.rotateShape(upperShape, facing));
        });
    }

    public LargeCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext selectionContext) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_SHAPES.get(state.getValue(FACING));
        } else {
            return UPPER_SHAPES.get(state.getValue(FACING));
        }
    }
}
