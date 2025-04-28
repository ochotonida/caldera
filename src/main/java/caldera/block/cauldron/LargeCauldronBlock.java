package caldera.block.cauldron;

import caldera.block.multiblock.CubeMultiblock;
import caldera.block.multiblock.state.Octant;
import caldera.block.multiblock.state.OrientationSet;
import caldera.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Unit;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LargeCauldronBlock extends CubeMultiblock<Unit> implements EntityBlock {

    public static final double FLOOR_HEIGHT = 4 / 16D;
    private static final double WALL_WIDTH = 2 / 16D;

    private static final Map<Octant, VoxelShape> SHAPES = new HashMap<>();

    static {
        VoxelShape lowerShape = Shapes.join(
                // start with a slab and a slightly wider cube on top
                Shapes.or(
                        Block.box(0, 2, 0, 16, 16, 16),
                        Block.box(0, 0, 0, 15, 2, 15)
                ),
                // subtract the basin from the first two shapes
                Block.box(0, 4, 0, 14, 16, 14),
                BooleanOp.ONLY_FIRST
        );

        VoxelShape upperShape = Shapes.join(
                // start with a slab
                Block.box(0, 0, 0, 16, 8, 16),
                // subtract the horizontal slits and the basin from the first shape
                Shapes.or(
                        Block.box(0, 0, 0, 14, 8, 14),
                        Block.box(15, 2, 0, 16, 5, 16),
                        Block.box(0, 2, 15, 16, 5, 16)
                ),
                BooleanOp.ONLY_FIRST
        );

        for (Octant octant : Octant.values()) {
            SHAPES.put(octant, VoxelShapeHelper.rotateShape(octant.isLower() ? lowerShape : upperShape, octant.getPrimaryFacing().get2DDataValue()));
        }
    }

    public LargeCauldronBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected OrientationSet<Unit> getOrientations() {
        return OrientationSet.UNIT;
    }

    @Nullable
    public CauldronBlockEntity getController(BlockState state, BlockPos pos, Level level) {
        BlockPos origin = getOrigin(state, pos);
        BlockState originState = level.getBlockState(origin);

        if (isOrigin(originState)) {
            BlockEntity blockEntity = level.getBlockEntity(origin);
            if (blockEntity instanceof CauldronBlockEntity) {
                return ((CauldronBlockEntity) blockEntity);
            }
        }

        return null;
    }

    public static boolean isOrigin(BlockState state) {
        return state.getBlock() instanceof LargeCauldronBlock && state.getValue(LargeCauldronBlock.OCTANT).getRelativePosition().equals(Vec3i.ZERO);
    }

    public static boolean isInsideCauldron(BlockState state, Vec3 vector) {
        return isInsideCauldron(state, vector.x(), vector.y(), vector.z());
    }

    // TODO test this
    public static boolean isInsideCauldron(BlockState state, double x, double y, double z) {
        double wallWidth = WALL_WIDTH - 0.0001;
        double floorHeight = FLOOR_HEIGHT - 0.0001;

        Direction.AxisDirection facingX = state.getValue(CubeMultiblock.OCTANT).getLocalX().getAxisDirection();
        Direction.AxisDirection facingZ = state.getValue(CubeMultiblock.OCTANT).getLocalZ().getAxisDirection();

        if (facingX == Direction.AxisDirection.NEGATIVE && x > 1 - wallWidth) {
            return false;
        } else if (facingX == Direction.AxisDirection.POSITIVE && x < wallWidth) {
            return false;
        }

        if (facingZ == Direction.AxisDirection.NEGATIVE && z > 1 - wallWidth) {
            return false;
        } else if (facingZ == Direction.AxisDirection.POSITIVE && z < wallWidth) {
            return false;
        }

        if (state.getValue(OCTANT).isLower()) {
            return y > floorHeight;
        }
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return (BlockEntityTicker<T>) CauldronBlockEntity.TICKER;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext selectionContext) {
        return SHAPES.get(state.getValue(OCTANT));
    }
}
