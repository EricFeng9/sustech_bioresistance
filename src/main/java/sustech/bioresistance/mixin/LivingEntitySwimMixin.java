package sustech.bioresistance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sustech.bioresistance.ModFluids;

/**
 * 注入LivingEntity类中与游泳、溺水相关的方法，
 * 确保土壤浸取液与水拥有相同的行为
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwimMixin extends Entity {

    public LivingEntitySwimMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // /**
    //  * 注入到canBreatheInWater方法，
    //  * 确保实体在土壤浸取液中也需要氧气（除非它能在水中呼吸）
    //  * 但只有当液体没过头部时才考虑溺水
    //  */
    // @Inject(method = "canBreatheInWater", at = @At("RETURN"), cancellable = true)
    // private void onCanBreatheInWater(CallbackInfoReturnable<Boolean> info) {
    //     // 如果实体原本就能在水中呼吸，不需要修改
    //     if (info.getReturnValueZ()) {
    //         return;
    //     }
        
    //     // 只有当土壤浸取液没过头部时才计算溺水
    //     // 检查实体头部位置的方块是否含有土壤浸取液
    //     BlockPos headPos = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
    //     FluidState fluidState = this.getWorld().getFluidState(headPos);
        
    //     if (fluidState.isOf(ModFluids.STILL_SOIL_EXTRACT) || fluidState.isOf(ModFluids.FLOWING_SOIL_EXTRACT)) {
    //         // 只有当头部在土壤浸取液中，实体才会溺水
    //         // 这样就实现了只有液体没过头部才开始计算溺水的逻辑
    //         info.setReturnValue(false);
    //     }
    // }
} 