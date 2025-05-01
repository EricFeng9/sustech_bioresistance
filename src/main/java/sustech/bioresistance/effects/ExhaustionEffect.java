package sustech.bioresistance.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

public class ExhaustionEffect extends StatusEffect {
    // 构造函数，接收效果类别(有害/有益/中性)和效果颜色
    public ExhaustionEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
    
    // 决定效果何时应用，返回true时会调用applyUpdateEffect
    // 这里设置每秒(20tick)检查一次
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }
    
    // 实际应用效果的逻辑
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            // 给予虚弱I效果，22tick持续时间(比检查周期稍长)
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS, 22, 0, false, true, true));
            
            // 给予挖掘疲劳I效果
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE, 22, 0, false, true, true));
            
            // 检查玩家是否在睡觉，如果是则移除过度疲劳效果
            if (player.isSleeping()) {
                player.removeStatusEffect(this);
            }
        }
    }
}