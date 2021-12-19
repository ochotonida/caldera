package caldera.common.block.cauldron;

import caldera.common.block.CubeMultiBlock;
import caldera.common.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LargeCauldronBlock extends CubeMultiBlock implements EntityBlock {

    private static final Map<Direction, VoxelShape> LOWER_SHAPES = new HashMap<>();
    private static final Map<Direction, VoxelShape> UPPER_SHAPES = new HashMap<>();

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

        Direction.Plane.HORIZONTAL.forEach(facing -> {
            LOWER_SHAPES.put(facing, VoxelShapeHelper.rotateShape(lowerShape, facing));
            UPPER_SHAPES.put(facing, VoxelShapeHelper.rotateShape(upperShape, facing));
        });
    }

    public LargeCauldronBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    public static CauldronBlockEntity getController(BlockState state, BlockPos pos, Level level) {
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
        return state.getBlock() instanceof LargeCauldronBlock && state.getValue(LargeCauldronBlock.FACING) == Direction.SOUTH
                && state.getValue(LargeCauldronBlock.HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isInsideCauldron(BlockState state, Vec3 vector) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CauldronBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // noinspection unchecked
        return (BlockEntityTicker<T>) CauldronBlockEntity.TICKER;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        CauldronBlockEntity blockEntity = getController(state, pos, level);
        if (blockEntity != null) {
            blockEntity.onPlayerAboutToDestroy(player);
            if (level.getBlockState(pos) != state) {
                return false;
            }
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public void onRemove(BlockState currentState, Level level, BlockPos replacedPos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && currentState.hasBlockEntity() && (!currentState.is(newState.getBlock()) || !newState.hasBlockEntity())) {
            if (level.getBlockEntity(replacedPos) instanceof CauldronBlockEntity cauldron) {
                cauldron.onRemove();
            }
        }
        super.onRemove(currentState, level, replacedPos, newState, isMoving);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext selectionContext) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_SHAPES.get(state.getValue(FACING));
        } else {
            return UPPER_SHAPES.get(state.getValue(FACING));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        Vec3 hitOffset = rayTraceResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        if (isInsideCauldron(state, hitOffset)) {
            CauldronBlockEntity controller = getController(state, pos, level);
            if (controller != null) {
                return controller.onUse(player, hand);
            }
        }

        return super.use(state, level, pos, player, hand, rayTraceResult);
    }
}
