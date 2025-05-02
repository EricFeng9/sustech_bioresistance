package sustech.bioresistance.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeModifierCreator;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import sustech.bioresistance.ModItems;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

/**
 * 破伤风效果类
 * 每次接触铁制工具后获得，效果为虚弱I 5min、缓慢I 5min
 * 倒计时结束后获得凋零效果直到死亡
 * 可以通过甲硝唑或抗耐药性微生物胶囊治愈
 */
public class TetanusEffect extends StatusEffect {
    
    // 常量定义
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160890");
    private static final float MOVEMENT_SPEED_MODIFIER = -0.15F; // 减速15%
    
    /**
     * 构造函数
     */
    public TetanusEffect() {
        super(
            StatusEffectCategory.HARMFUL, // 有害效果类别
            0x5A3A1A                     // 效果颜色 (棕色)
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
        // 检查效果剩余时间
        StatusEffectInstance tetanusInstance = entity.getStatusEffect(this);
        int sendMessageCnt = 0;
        // 如果破伤风效果快要结束（剩余时间小于0.5秒）且实体还活着
        if (tetanusInstance != null && tetanusInstance.getDuration() <= 10 && entity.isAlive()) {
            
            // 如果是玩家，发送消息提示进入急性期
            if (entity instanceof PlayerEntity player && sendMessageCnt == 0) {
                // 获取玩家的语言设置，创建本地化消息
                Text message = Text.translatable("tetanus.acute_phase")
                    .append(Text.translatable("tetanus.acute_phase.incurable").formatted(net.minecraft.util.Formatting.RED));
                
                // 发送消息给玩家
                player.sendMessage(message, false);
                sendMessageCnt++;
            }
            
            // 直接杀死实体，而不是添加凋零效果
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
     * 覆盖此方法使破伤风只能被特定物品治愈
     */
    @Override
    public boolean isBeneficial() {
        return false; // 非有益效果
    }
} 