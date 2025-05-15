package sustech.bioresistance.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * 鼠疫效果类
 * 可以通过被老鼠攻击或食用生老鼠肉获得
 * 效果为虚弱、缓慢，持续5分钟，倒计时结束后直接死亡
 * 可以通过链霉素或抗耐药性微生物胶囊治愈
 */
public class PlagueEffect extends StatusEffect {
    
    // 常量定义
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("9823AB34-7FE1-4B5A-90DE-347C1F160891");
    private static final float MOVEMENT_SPEED_MODIFIER = -0.2F; // 减速20%
    private static final int DEATH_THRESHOLD = 10; // 当剩余时间小于0.5秒(10tick)时直接死亡
    
    /**
     * 构造函数
     */
    public PlagueEffect() {
        super(
            StatusEffectCategory.HARMFUL, // 有害效果类别
            0x4D3900                     // 效果颜色 (深褐色)
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
        // 每5秒造成1点伤害
        if (entity.age % 100 == 0) {
            entity.damage(entity.getDamageSources().magic(), 1.0f);
        }
        
        // 检查效果剩余时间
        StatusEffectInstance plagueInstance = entity.getStatusEffect(this);
        
        // 如果鼠疫效果快要结束（剩余时间小于0.5秒）且实体还活着
        if (plagueInstance != null && plagueInstance.getDuration() <= DEATH_THRESHOLD && entity.isAlive()) {
            // 如果是玩家，发送消息提示进入急性期
            if (entity instanceof PlayerEntity player) {
                // 创建本地化消息
                Text message = Text.translatable("plague.acute_phase")
                    .append(Text.translatable("plague.acute_phase.fatal").formatted(net.minecraft.util.Formatting.RED));
                
                // 发送消息给玩家
                player.sendMessage(message, false);
            }
            
            // 直接杀死实体
            entity.damage(entity.getDamageSources().magic(), Float.MAX_VALUE);
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
     * 覆盖此方法使鼠疫只能被特定物品治愈
     */
    @Override
    public boolean isBeneficial() {
        return false; // 非有益效果
    }
} 