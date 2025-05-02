package sustech.bioresistance.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import sustech.bioresistance.ModFluids;

/**
 * 土壤浸取液流体
 * 具有与水相似的特性，但是是从泥土和菌丝中提取的营养液体
 */
public abstract class SoilExtractFluid extends FlowableFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_SOIL_EXTRACT;
    }

    @Override
    public Fluid getStill() {
        return ModFluids.STILL_SOIL_EXTRACT;
    }

    @Override
    public Item getBucketItem() {
        return ModFluids.SOIL_EXTRACT_BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState fluidState) {
        return ModFluids.SOIL_EXTRACT_FLUID_BLOCK.getDefaultState()
                .with(FluidBlock.LEVEL, getBlockStateLevel(fluidState));
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == ModFluids.STILL_SOIL_EXTRACT || fluid == ModFluids.FLOWING_SOIL_EXTRACT;
    }
    
    // 流体设置为无限
    @Override
    protected boolean isInfinite(World world) {
        return true;
    }

    /**
     * 静止的土壤浸取液
     */
    public static class Still extends SoilExtractFluid {

        @Override
        protected boolean isInfinite(World world) {
            return true; // 无限水源
        }

        @Override
        protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
            // 空实现，不需要特殊处理
        }

        @Override
        protected int getFlowSpeed(WorldView world) {
            return 0; // 静止状态无流速
        }

        @Override
        protected int getLevelDecreasePerBlock(WorldView world) {
            return 1; // 每个方块减少1级
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos,
                                            Fluid fluid, Direction direction) {
            return false; // 不允许被替换
        }

        @Override
        public int getLevel(FluidState state) {
            return 8; // 满格源块
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        protected float getBlastResistance() {
            return 100.0F; // 防爆能力，与水相同
        }

        @Override
        public int getTickRate(WorldView world) {
            return 20; // 更新速度较慢
        }

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }
    }

    /**
     * 流动的土壤浸取液
     */
    public static class Flowing extends SoilExtractFluid {

        @Override
        protected boolean isInfinite(World world) {
            return true; // 无限水源
        }

        @Override
        protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
            // 空实现，不需要特殊处理
        }

        @Override
        protected int getFlowSpeed(WorldView world) {
            return 4; // 与水相同的流速
        }

        @Override
        protected int getLevelDecreasePerBlock(WorldView world) {
            return 1; // 每个方块减少1级
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos,
                                            Fluid fluid, Direction direction) {
            return false; // 不允许被替换
        }

        @Override
        protected float getBlastResistance() {
            return 100.0F; // 防爆能力，与水相同
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        @Override
        public int getTickRate(WorldView world) {
            return 5; // 流动更新频率，与水相同
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }
    }
} 