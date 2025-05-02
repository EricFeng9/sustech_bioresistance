package sustech.bioresistance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sustech.bioresistance.ModFluids;

/**
 * 注入Entity类中处理流体相关的方法
 * 确保土壤浸取液被正确识别为可游泳和可溺水的流体
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    
    @Shadow public abstract Vec3d getPos();
    @Shadow public abstract World getWorld();
    @Shadow public abstract double getEyeY();
    @Shadow public abstract boolean isSubmergedIn(TagKey<Fluid> tag);
    
    /**
     * 注入isSubmergedIn方法，确保土壤浸取液被正确识别为可游泳的流体
     * 此方法用于判断实体是否接触流体，影响游泳效果
     */
    @Inject(method = "isSubmergedIn", at = @At("HEAD"), cancellable = true)
    private void onIsSubmergedIn(TagKey<Fluid> fluidTag, CallbackInfoReturnable<Boolean> info) {
        if (fluidTag == FluidTags.WATER) {
            // 检查实体头部位置是否有土壤浸取液
            BlockPos headPos = BlockPos.ofFloored(this.getPos().x, this.getEyeY(), this.getPos().z);
            
            FluidState fluidState = this.getWorld().getFluidState(headPos);
            
            if (fluidState.isOf(ModFluids.STILL_SOIL_EXTRACT) || fluidState.isOf(ModFluids.FLOWING_SOIL_EXTRACT)) {
                info.setReturnValue(true); // 只有头部在土壤浸取液中才认为在水中
            }
        }
    }
    
    // /**
    //  * 注入isSubmergedInWater方法，确保只有当实体头部浸没在土壤浸取液中时才返回true
    //  * 这是溺水计算的关键方法，只有此方法返回true时才会开始计算溺水
    //  */
    // @Inject(method = "isSubmergedInWater", at = @At("HEAD"), cancellable = true)
    // private void onIsSubmergedInWater(CallbackInfoReturnable<Boolean> info) {
    //     // 检查实体头部位置是否在土壤浸取液中
    //     BlockPos headPos = BlockPos.ofFloored(this.getPos().x, this.getEyeY(), this.getPos().z);
    //     FluidState fluidAtHead = this.getWorld().getFluidState(headPos);
        
    //     // 只有当头部在土壤浸取液中，才触发溺水计算
    //     if (fluidAtHead.isOf(ModFluids.STILL_SOIL_EXTRACT) || fluidAtHead.isOf(ModFluids.FLOWING_SOIL_EXTRACT)) {
    //         info.setReturnValue(true);
    //     }
    // }
}