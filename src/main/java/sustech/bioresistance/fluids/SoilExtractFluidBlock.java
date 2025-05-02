package sustech.bioresistance.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * 土壤浸取液流体方块，支持游泳和溺水
 * 与水完全相同的行为，但只有液体没过头部时才会触发溺水
 */
public class SoilExtractFluidBlock extends FluidBlock {

    public SoilExtractFluidBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    /**
     * 实体接触流体方块时的行为
     * 确保与默认水方块完全相同的行为，但只有液体没过头部时才会触发溺水
     */
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // 先调用父类方法实现与水相同的碰撞行为（上浮、阻力等）
        super.onEntityCollision(state, world, pos, entity);
        
        // 对于生物实体，我们需要检查是否完全浸没
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            
            // 通常情况下，土壤浸取液的行为由以下方面处理：
            // 1. 由于我们添加了土壤浸取液到water流体标签，它会被游戏识别为"水"
            // 2. Entity/LivingEntity的原生方法会处理游泳状态和移动
            // 3. 我们的EntityMixin和LivingEntitySwimMixin会确保溺水逻辑只在完全浸没时触发
            
            // 对于此方法，我们无需额外操作
            // 因为我们已经在Mixin中修改了isSubmergedInWater方法的行为
        }
    }
    
    /**
     * 检查实体是否完全浸没在流体中（包括头部）
     * 这只是一个辅助方法，主要逻辑已经被我们的Mixin处理
     */
    private boolean isCompletelySubmerged(Entity entity, World world) {
        // 检查实体头部是否也在流体中
        double eyeY = entity.getY() + entity.getEyeHeight(entity.getPose());
        BlockPos headPos = BlockPos.ofFloored(entity.getX(), eyeY, entity.getZ());
        return world.getFluidState(headPos).isIn(FluidTags.WATER);
    }
} 