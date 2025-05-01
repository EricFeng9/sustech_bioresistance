package sustech.bioresistance.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sustech.bioresistance.ModStatusEffects;

@Mixin(PlayerEntity.class) // 注入到PlayerEntity类
public class PlayerEntityMixin {
    
    // 在每个tick结束时执行我们的代码
    @Inject(method = "tick", at = @At("TAIL"))
    private void checkExhaustion(CallbackInfo ci) {
        // 获取当前玩家实例
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // 如果已经有过度疲劳效果，直接返回
        if (player.hasStatusEffect(ModStatusEffects.EXHAUSTION)) {
            return;
        }
        
        // 必须是服务器端玩家才能获取统计信息
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 获取玩家距离上次睡觉的时间(tick)
            int timeSinceRest = serverPlayer.getStatHandler().getStat(
                Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
            
            // 判断是否超过3个游戏日(3×24000=72000 tick)
            if (timeSinceRest >= 72000) {
                // 添加过度疲劳效果，持续无限长，等级0(I级)
                player.addStatusEffect(new StatusEffectInstance(
                        ModStatusEffects.EXHAUSTION, // 我们的自定义效果
                        Integer.MAX_VALUE, // 持续到玩家睡觉(效果类中会自动移除)
                        0, // 效果等级(0=I级)
                        false, // 不是来自信标
                        true, // 显示粒子效果
                        true // 在GUI中显示图标
                ));
            }
        }
    }
}