package sustech.bioresistance;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sustech.bioresistance.effects.ExhaustionEffect;
import sustech.bioresistance.effects.PlagueEffect;
import sustech.bioresistance.effects.TetanusEffect;

public class ModStatusEffects {
    // 注册过度劳累效果
    public static final StatusEffect EXHAUSTION = new ExhaustionEffect(
            StatusEffectCategory.HARMFUL, // 有害效果
            0x555555 // 深灰色
    );
    
    // 注册破伤风效果
    public static final StatusEffect TETANUS = new TetanusEffect();
    
    // 注册鼠疫效果
    public static final StatusEffect PLAGUE = new PlagueEffect();

    // 初始化方法
    public static void initialize() {
        // 注册效果到Minecraft的注册表
        Registry.register(
                Registries.STATUS_EFFECT, // 状态效果注册表
                new Identifier(Bioresistance.MOD_ID, "exhaustion"), // 唯一ID
                EXHAUSTION // 我们的效果实例
        );
        
        // 注册破伤风效果
        Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(Bioresistance.MOD_ID, "tetanus"), // 唯一ID为"tetanus"
                TETANUS // 破伤风效果实例
        );
        
        // 注册鼠疫效果
        Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(Bioresistance.MOD_ID, "plague"), // 唯一ID为"plague"
                PLAGUE // 鼠疫效果实例
        );
        
        Bioresistance.LOGGER.info("注册状态效果");
    }
} 