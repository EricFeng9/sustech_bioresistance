package sustech.bioresistance.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * 自定义油流体方块，支持连锁爆炸
 */
public class Oil_Fluid_Block extends FluidBlock {
    // 用于在BFS中遍历上下左右前后六个方位
    private static final Direction[] DIRECTIONS = new Direction[]{
            Direction.UP, Direction.DOWN,
            Direction.NORTH, Direction.SOUTH,
            Direction.EAST, Direction.WEST
    };

    public Oil_Fluid_Block(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    /**
     * 当方块(含流体方块)与相邻方块交互变化时被调用
     * 例如：周围出现火、岩浆，或者有方块被破坏/放置等。
     */
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos,
                               Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        if (!world.isClient) {
            // 只检查"触发更新"的邻居方块sourcePos
            // 如果那个邻居是火或岩浆等危险方块，就引发连锁爆炸
            Block neighborBlock = world.getBlockState(sourcePos).getBlock();
            if (isDangerBlock(neighborBlock)) {
                chainExplosion((ServerWorld) world, pos);
            }

            /**
             * 如果你想要更加激进：只要油周围任何一个邻居是火/岩浆就爆炸，可遍历6面：
             *
             * for (Direction dir : DIRECTIONS) {
             *     BlockPos nPos = pos.offset(dir);
             *     Block nbBlock = world.getBlockState(nPos).getBlock();
             *     if (isDangerBlock(nbBlock)) {
             *         chainExplosion((ServerWorld) world, pos);
             *         break;
             *     }
             * }
             */
        }
    }

    /**
     * 当实体和油流体碰撞时触发（实体每个 tick 都可能调用）
     */
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient) {
            // 火焰弹、烟花、或实体本身着火，会触发连锁爆炸
            if (entity instanceof FireballEntity || entity instanceof FireworkRocketEntity) {
                chainExplosion((ServerWorld) world, pos);
            } else if (entity.isOnFire()) {
                chainExplosion((ServerWorld) world, pos);
            }
        }

        // 对活体生物施加负面效果（油窒息、不便移动等）
        if (entity instanceof LivingEntity living) {
            applyOilEffects(living);
        }
    }

    /**
     * 对进入油的生物施加一系列状态效果
     */
    private static void applyOilEffects(LivingEntity entity) {
        // 1. 致盲
        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.BLINDNESS, 60, 0, false, false, true
        ));
        // 2. 减速
        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, 60, 1, false, false, true
        ));
        // 3. 枯萎(中毒式掉血)
        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WITHER, 60, 0, false, false, true
        ));
    }

    /**
     * 判断是否是危险方块（如火、岩浆等）
     */
    private boolean isDangerBlock(Block block) {
        // 如果你想包含更多方块(例如火把之类)，可自行拓展
        return (block instanceof FireBlock)
                || (block == Blocks.FIRE)
                || (block == Blocks.LAVA)
                || (block == Blocks.SOUL_FIRE);
    }

    /**
     * ======================================
     * 连锁爆炸的核心方法
     * 1) 找到所有相连的油方块 (BFS 搜索)
     * 2) 把它们都移除
     * 3) 每个油方块位置都触发爆炸(或只在起始点爆炸)
     * ======================================
     */
    private void chainExplosion(ServerWorld world, BlockPos startPos) {
        // 1. 找到所有相连的油方块位置
        Set<BlockPos> oilCluster = findConnectedOilBlocks(world, startPos);

        // 2. 逐个方块移除并爆炸
        //   如果你只想在原点"startPos"做一次大爆炸，那就只对 startPos/createExplosion；
        //   如果想每个都爆，就循环。
        for (BlockPos oilPos : oilCluster) {
            world.setBlockState(oilPos, Blocks.AIR.getDefaultState(), 3);

            // 在这里决定爆炸方式：可以每个都来一次 TNT 级爆炸（4.0F）
            world.createExplosion(
                    null,
                    oilPos.getX() + 0.5,
                    oilPos.getY() + 0.5,
                    oilPos.getZ() + 0.5,
                    2.0F,
                    World.ExplosionSourceType.BLOCK
            );
        }
    }

    /**
     * BFS：寻找与 startPos 相连的所有油方块(含 startPos 本身)
     * 这里判断方法：如果 world.getBlockState(...) 的 Block == this
     * 就认为它是同一种油方块。
     */
    private Set<BlockPos> findConnectedOilBlocks(ServerWorld world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // 先把起始坐标放入队列
        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            // 遍历 current 周围6个方块
            for (Direction dir : DIRECTIONS) {
                BlockPos neighbor = current.offset(dir);

                // 安全性：如果这个区块已卸载或超出世界边界，得跳过
                if (!world.isInBuildLimit(neighbor)) {
                    continue;
                }

                // 如果邻居也是同一个油方块，加入队列
                BlockState neighborState = world.getBlockState(neighbor);
                if (neighborState.getBlock() == this) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return visited;
    }
}