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
 * 自定义油流体的总抽象类 (含 "Still" 和 "Flowing" 两个子类)。
 *
 * 注意：FluidBlock.LEVEL 默认范围是 [1..8]，
 *  - Still fluid 通常用 level=8 表示“满格源块”。
 *  - Flowing fluid 常用 1..7 (当流到8时会转化为Still)。
 */
public abstract class Oil_Fluid extends FlowableFluid {

    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_OIL; // 请确保 ModFluids 里注册了此实例
    }

    @Override
    public Fluid getStill() {
        return ModFluids.STILL_OIL;   // 确保 ModFluids 里注册了此实例
    }

    @Override
    public Item getBucketItem() {
        return ModFluids.OIL_BUCKET;  // 你自定义的油桶物品
    }


    /**
     * toBlockState 是将 流体的state 映射到对应的BlockState (FluidBlock.LEVEL)。
     */
    @Override
    protected BlockState toBlockState(FluidState fluidState) {
        // 默认使用 FluidBlock.LEVEL (范围[1..8])
        return ModFluids.OIL_FLUID_BLOCK.getDefaultState()
                .with(FluidBlock.LEVEL, getBlockStateLevel(fluidState));
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        // 如果 STILL_OIL 和 FLOWING_OIL 都是同一种油，只是状态不同，则需要在这里把它们都视为同一种
        return fluid == ModFluids.STILL_OIL || fluid == ModFluids.FLOWING_OIL;
    }


    // ─────────────────────────────────────────────────────
    // 1) 静止油 - Oil_Fluid_Still
    // ─────────────────────────────────────────────────────
    public static class Oil_Fluid_Still extends Oil_Fluid {

        @Override
        protected boolean isInfinite(World world) {
            return false;
        }

        @Override
        protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
            // 可留空
        }

        // 静止：不流动 => flowSpeed=0
        @Override
        protected int getFlowSpeed(WorldView world) {
            return 0;
        }

        // 会减少多少层，可保留1
        @Override
        protected int getLevelDecreasePerBlock(WorldView world) {
            return 1;
        }

        // 不允许被另外的fluid替换
        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos,
                                            Fluid fluid, Direction direction) {
            return false;
        }

        // 静止：视作“满格源块” => level=8
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }

        @Override
        protected float getBlastResistance() {
            return 0;
        }

        // tickRate 可大些，减少频繁更新
        @Override
        public int getTickRate(WorldView world) {
            return 20;
        }

        // 关键：把 FlowableFluid.LEVEL 注册到本Fluid的state
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(FlowableFluid.LEVEL);
        }

    }

    // ─────────────────────────────────────────────────────
    // 2) 流动油 - Oil_Fluid_Flowing
    // ─────────────────────────────────────────────────────
    public static class Oil_Fluid_Flowing extends Oil_Fluid {

        @Override
        protected boolean isInfinite(World world) {
            return false;
        }

        @Override
        protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
            // 可留空
        }

        // 有一定流速
        @Override
        protected int getFlowSpeed(WorldView world) {
            return 4;
        }

        @Override
        protected int getLevelDecreasePerBlock(WorldView world) {
            return 1;
        }

        @Override
        protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos,
                                            Fluid fluid, Direction direction) {
            return false;
        }

        @Override
        protected float getBlastResistance() {
            return 0;
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }

        // 流动更新频率
        @Override
        public int getTickRate(WorldView world) {
            return 5;
        }

        /**
         * Flowing fluid：通常 level=1..7 (最终达到8会自动变成Still)
         */
        @Override
        public int getLevel(FluidState state) {
            return state.get(FlowableFluid.LEVEL);
        }

        // 关键：把 FlowableFluid.LEVEL 注册到本Fluid的state
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(FlowableFluid.LEVEL);
        }
    }
}