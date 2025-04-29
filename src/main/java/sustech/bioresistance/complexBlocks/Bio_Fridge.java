package sustech.bioresistance.complexBlocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import sustech.bioresistance.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class Bio_Fridge extends BlockWithEntity {
    // 定义水平 FACING 属�?
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    // 构造函�?
    public Bio_Fridge(Settings settings) {
        super(settings);
        // 设置默认方向�? NORTH
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    //设置碰撞�?
    public VoxelShape getShape(BlockState state){
        Direction facing = state.get(FACING);
        switch (facing) {
            case NORTH:
                return Block.createCuboidShape(0, 0, 0, 32, 24, 16); // 朝北放置
            case SOUTH:
                return Block.createCuboidShape(-16, 0, 0, 16, 24, 16); // 朝南放置
            case EAST:
                return Block.createCuboidShape(0, 0, 0, 16, 24, 32); // 朝东放置
            case WEST:
                return Block.createCuboidShape(0, 0, -16, 16, 24, 16); // 朝西放置
            default:
                return Block.createCuboidShape(0, 0, 0, 32, 24, 16); // 默认朝北放置
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

    public static void initialize(){}

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

    @Override
    public boolean isEnabled(FeatureSet enabledFeatures) {
        return super.isEnabled(enabledFeatures);
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockRenderView renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return super.getAppearance(state, renderView, pos, side, sourceState, sourcePos);
    }

    // 关联方块实体
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Bio_Fridge_Entity(pos, state);
    }
    // 当玩家右击方块时打开 GUI
    /**
     * 当玩家右键点击方块时，让玩家打开对应方块实体提供�? ScreenHandler
     */
    @Override
    public ActionResult onUse(BlockState state,
                              World world,
                              BlockPos pos,
                              PlayerEntity player,
                              Hand hand,
                              BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof Bio_Fridge_Entity) {
                player.openHandledScreen((Bio_Fridge_Entity) blockEntity);
            }
        }
        return ActionResult.SUCCESS;
    }
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        // 确保返回 MODEL，而不要返�? INVISIBLE
        return BlockRenderType.MODEL;
    }
}