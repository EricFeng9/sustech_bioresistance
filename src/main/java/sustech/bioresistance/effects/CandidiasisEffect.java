package sustech.bioresistance.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * 耳念珠菌感染效果类
 * 当玩家处于【过度劳累】状态，或有以下状态的其中之一：【鼠疫】【破伤风】时，有小概率被感染
 * 效果为虚弱、缓慢，持续3分钟，倒计时结束后先触发反胃效果20秒，然后直接死亡
 * 可以通过抗真菌药或抗耐药性微生物软膏治愈
 */
public class CandidiasisEffect extends StatusEffect {
    
    // 常量定义
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("A7B3C652-8FE1-4D5A-90DE-347C1F160892");
    private static final float MOVEMENT_SPEED_MODIFIER = -0.15F; // 减速15%
    private static final int ACUTE_PHASE_DURATION = 400; // 急性期（反胃效果）持续20秒(400tick)
    private static final int DEATH_THRESHOLD = 20; // 当急性期剩余时间小于1秒(20tick)时直接死亡
    
    /**
     * 构造函数
     */
    public CandidiasisEffect() {
        super(
            StatusEffectCategory.HARMFUL, // 有害效果类别
            0xEDDCA5                     // 效果颜色 (浅黄色)
        );
        
        // 添加属性修饰符，减少移动速度
        this.addAttributeModifier(
            EntityAttributes.GENERIC_MOVEMENT_SPEED,  // 影响移动速度属性
            MOVEMENT_SPEED_MODIFIER_ID.toString(),    // 修饰符ID
            MOVEMENT_SPEED_MODIFIER,                  // 修饰符数值
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL // 操作类型为乘法
        );
    }
    
    /**
     * 每个游戏刻调用的方法，用于应用效果
     * @param entity 受影响的实体
     * @param amplifier 效果等级
     */
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // 获取效果状态实例
        StatusEffectInstance candidiasisInstance = entity.getStatusEffect(this);
        
        if (candidiasisInstance != null) {
            int duration = candidiasisInstance.getDuration();
            
            // 当剩余时间小于等于急性期持续时间时（3分钟结束后）
            if (duration <= ACUTE_PHASE_DURATION && duration > DEATH_THRESHOLD) {
                // 如果玩家还没有反胃效果，添加反胃效果
                if (!entity.hasStatusEffect(StatusEffects.NAUSEA)) {
                    entity.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA, // 反胃效果
                        ACUTE_PHASE_DURATION, // 持续20秒
                        0,                    // 效果等级 0 (I级)
                        false,                // 不显示粒子
                        true,                 // 显示图标
                        true                  // 可以被治愈
                    ));
                    
                    // 如果是玩家，发送消息提示进入急性期
                    if (entity instanceof PlayerEntity player) {
                        // 创建本地化消息
                        Text message = Text.translatable("candidiasis.acute_phase")
                            .append(Text.translatable("candidiasis.acute_phase.fatal").formatted(net.minecraft.util.Formatting.RED));
                        
                        // 发送消息给玩家
                        player.sendMessage(message, true);
                    }
                }
            }
            // 当急性期（反胃效果）快要结束时直接杀死实体
            else if (duration <= DEATH_THRESHOLD && entity.isAlive()) {
                // 直接杀死实体
                entity.damage(entity.getDamageSources().magic(), Float.MAX_VALUE);
            }
        }
    }
    
    /**
     * 确定是否每个游戏刻都应用效果
     * @return 是否每刻更新
     */
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // 每个游戏刻都检查效果状态
    }
    
    /**
     * 控制效果是否可以被治愈
     * 默认情况下，喝牛奶可以治愈任何效果
     * 覆盖此方法使耳念珠菌感染只能被特定物品治愈
     */
    @Override
    public boolean isBeneficial() {
        return false; // 非有益效果
    }
} 