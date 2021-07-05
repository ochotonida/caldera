package caldera.common.block.cauldron;

import caldera.common.block.CubeMultiBlock;
import caldera.common.util.VoxelShapeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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

    @Nullable
    public static CauldronBlockEntity getController(BlockState state, BlockPos pos, World level) {
        BlockPos origin = getOrigin(state, pos);
        BlockState originState = level.getBlockState(origin);

        if (isOrigin(originState)) {
            TileEntity blockEntity = level.getBlockEntity(origin);
            if (blockEntity instanceof CauldronBlockEntity) {
                return ((CauldronBlockEntity) blockEntity);
            }
        }

        return null;
    }

    public static boolean isOrigin(BlockState state) {
        return state.getBlock() instanceof LargeCauldronBlock && state.getValue(LargeCauldronBlock.FACING) == Direction.SOUTH
                && state.getValue(LargeCauldronBlock.HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isInsideCauldron(BlockState state, Vector3d vector) {
        return isInsideCauldron(state, vector.x(), vector.y(), vector.z());
    }

    private static boolean isInsideCauldron(BlockState state, double x, double y, double z) {
        double wallWidth = 1.99 / 16D;
        double floorHeight = 3.99 / 16D;

        Direction.AxisDirection facingX = CubeMultiBlock.getFacing(state, Direction.Axis.X).getAxisDirection();
        Direction.AxisDirection facingZ = CubeMultiBlock.getFacing(state, Direction.Axis.Z).getAxisDirection();

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

        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return y > floorHeight;
        }
        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CauldronBlockEntity();
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

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        // only handle this for the block that contains the center of the entity,
        // to prevent this from being called multiple times per tick per cauldron
        if (!entity.blockPosition().equals(pos)) {
            return;
        }

        // only consider entities that are actually in the basin part of the cauldron
        double xOffset = entity.position().x - pos.getX();
        double yOffset = entity.position().y - pos.getY();
        double zOffset = entity.position().z - pos.getZ();
        if (!isInsideCauldron(state, xOffset, yOffset, zOffset)) {
            return;
        }

        CauldronBlockEntity controller = getController(state, pos, level);
        if (controller != null) {
            double floorHeight = 4 / 16D;
            double height = entity.position().y - (int) entity.position().y - floorHeight;
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                height += 1;
            }

            controller.onEntityInside(entity, height);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        Vector3d hitOffset = rayTraceResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        if (isInsideCauldron(state, hitOffset)) {
            CauldronBlockEntity controller = getController(state, pos, level);
            if (controller != null) {
                return controller.onUse(player, hand);
            }
        }

        return super.use(state, level, pos, player, hand, rayTraceResult);
    }
}
