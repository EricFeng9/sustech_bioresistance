package sustech.bioresistance.complexBlocks;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import sustech.bioresistance.ModBlocks;
import sustech.bioresistance.ModEntityTypes;

import java.util.ArrayList;
import java.util.List;

public class CleanTable extends BlockWithEntity {
    public CleanTable(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }
    // 定义水平 FACING 属�?
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    //设置碰撞�?
    public VoxelShape getShape(BlockState state){
        Direction facing = state.get(FACING);
        switch (facing) {
            case NORTH:
                return Block.createCuboidShape(0, 0, 0, 32, 16, 16); // 朝北放置
            case SOUTH:
                return Block.createCuboidShape(-16, 0, 0, 16, 16, 16); // 朝南放置
            case EAST:
                return Block.createCuboidShape(0, 0, 0, 16, 16, 32); // 朝东放置
            case WEST:
                return Block.createCuboidShape(0, 0, -16, 16, 16, 16); // 朝西放置
            default:
                return Block.createCuboidShape(0, 0, 0, 32, 16, 16); // 默认朝北放置
        }
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getShape(state);
    }

    // 确保 FACING 属性被注册
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 覆盖 getPlacementState 方法，根据玩家朝向设置水平方�?
    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        // 获取玩家的水平朝�?
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing());
    }
    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CleanTable_Entity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) { // 仅在服务器端执行
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CleanTable_Entity) {
                player.openHandledScreen((ExtendedScreenHandlerFactory) blockEntity);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CleanTable_Entity) {
                ItemScatterer.spawn(world, pos, (CleanTable_Entity) blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;

    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(
                type,
                ModEntityTypes.CleanTable, // 确保此处�? CleanTable_Entity 的注册类�?
                CleanTable_Entity::tick
        );
    }

    private static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> checkType(
            BlockEntityType<T> givenType,
            BlockEntityType<E> expectedType,
            BlockEntityTicker<? super E> ticker
    ) {
        return expectedType == givenType ? (BlockEntityTicker<T>) ticker : null;
    }


    //解决叠放问题------
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // 检查当前格子是否为�?
        if (!world.getBlockState(pos).isAir()) {
            return false;
        }

        // 获取方块占用范围
        List<BlockPos> occupiedPositions = getOccupiedPositions(pos, state);
        for (BlockPos occupiedPos : occupiedPositions) {
            if (!world.getBlockState(occupiedPos).isAir()) {
                return false; // 如果占用范围内有方块，阻止放�?
            }
        }
        return true; // 允许放置
    }

    public List<BlockPos> getOccupiedPositions(BlockPos pos, BlockState state) {
        List<BlockPos> positions = new ArrayList<>();
        Direction facing = state.get(FACING);

        switch (facing) {
            case NORTH:
                positions.add(pos.east());
                positions.add(pos.up());
                positions.add(pos.up().east());
                break;
            case SOUTH:
                positions.add(pos.west());
                positions.add(pos.up());
                positions.add(pos.up().west());
                break;
            case EAST:
                positions.add(pos.south());
                positions.add(pos.up());
                positions.add(pos.up().south());
                break;
            case WEST:
                positions.add(pos.north());
                positions.add(pos.up());
                positions.add(pos.up().north());
                break;
            default:
                break;
        }

        return positions;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        {
            // 获取占用的额外格�?
            List<BlockPos> occupiedPositions = getOccupiedPositions(pos, state);

            // 将这些格子设置为占用状态（可以用自定义方块或简单填充空气）
            for (BlockPos occupiedPos : occupiedPositions) {
                world.setBlockState(occupiedPos, ModBlocks.OCCUPIED_BLOCK.getDefaultState());
            }
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        {
            // 获取占用的额外格�?
            List<BlockPos> occupiedPositions = getOccupiedPositions(pos, state);

            // 清理占用的格�?
            for (BlockPos occupiedPos : occupiedPositions) {
                world.removeBlock(occupiedPos, false); // 移除占用方块
            }
        }
        return state;
    }
    //------
}
