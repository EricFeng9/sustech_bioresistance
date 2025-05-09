package sustech.bioresistance.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sustech.bioresistance.events.PlayerHurtCallback;
import sustech.bioresistance.ModStatusEffects;

/**
 * 生物实体受伤Mixin
 * 用于监听实体受伤事件并触发自定义事件
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * 注入到damage方法，在伤害计算前触发事件
     * @param source 伤害源
     * @param amount 伤害数值
     * @param info 回调信息
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        ActionResult result = PlayerHurtCallback.EVENT.invoker().onHurt((LivingEntity) (Object) this, source, amount);

        if (result == ActionResult.FAIL) {
            info.setReturnValue(false);
        }
    }

    /**
     * 拦截移除所有效果的方法
     * 防止破伤风和鼠疫被牛奶等物品移除
     * @param callback 回调信息
     */
    @Inject(method = "clearStatusEffects", at = @At("HEAD"), cancellable = true)
    private void onClearStatusEffects(CallbackInfoReturnable<Boolean> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // 检查特殊效果
        boolean hasTetanus = entity.hasStatusEffect(ModStatusEffects.TETANUS);
        boolean hasPlague = entity.hasStatusEffect(ModStatusEffects.PLAGUE);
        
        // 如果实体有破伤风或鼠疫效果，需要特殊处理
        if (hasTetanus || hasPlague) {
            // 保存特殊效果
            StatusEffectInstance tetanusEffect = null;
            StatusEffectInstance plagueEffect = null;
            
            if (hasTetanus) {
                tetanusEffect = entity.getStatusEffect(ModStatusEffects.TETANUS);
            }
            
            if (hasPlague) {
                plagueEffect = entity.getStatusEffect(ModStatusEffects.PLAGUE);
            }
            
            // 临时变量，记录是否有其他效果被清除
            boolean otherEffectsCleared = false;
            
            // 先清除除了破伤风和鼠疫以外的所有效果
            for (StatusEffect effect : entity.getActiveStatusEffects().keySet().toArray(new StatusEffect[0])) {
                if (effect != ModStatusEffects.TETANUS && effect != ModStatusEffects.PLAGUE) {
                    entity.removeStatusEffect(effect);
                    otherEffectsCleared = true;
                }
            }
            
            // 返回是否有效果被清除的结果
            callback.setReturnValue(otherEffectsCleared);
            callback.cancel();
        }
    }
    
    /**
     * 拦截移除单个效果的方法
     * 防止破伤风和鼠疫效果被牛奶等物品移除
     * @param effect 要移除的效果
     * @param callback 回调信息
     */
    @Inject(method = "removeStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onRemoveStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> callback) {
        // 如果尝试移除的是破伤风或鼠疫效果，阻止操作
        if (effect == ModStatusEffects.TETANUS || effect == ModStatusEffects.PLAGUE) {
            // 仅允许从PlagueEventHandler或TetanusEventHandler中主动调用的移除操作
            // 这是通过调用栈判断的，不太完美但可用
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            boolean allowedCaller = false;
            
            // 检查调用栈中是否有我们的事件处理类
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (className.contains("TetanusEventHandler") || 
                    className.contains("PlagueEventHandler") ||
                    className.contains("ModStatusEffects")) {
                    allowedCaller = true;
                    break;
                }
            }
            
            // 如果不是允许的调用者，拒绝移除操作
            if (!allowedCaller) {
                callback.setReturnValue(false);
                callback.cancel();
            }
        }
    }
} 