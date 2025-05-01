package sustech.bioresistance;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sustech.bioresistance.effects.ExhaustionEffect;

public class ModStatusEffects {
    // 注册过度劳累效果
    public static final StatusEffect EXHAUSTION = new ExhaustionEffect(
            StatusEffectCategory.HARMFUL, // 有害效果
            0x555555 // 深灰色
    );

    // 初始化方法
    public static void initialize() {
        // 注册效果到Minecraft的注册表
        Registry.register(
                Registries.STATUS_EFFECT, // 状态效果注册表
                new Identifier(Bioresistance.MOD_ID, "exhaustion"), // 唯一ID
                EXHAUSTION // 我们的效果实例
        );
        
        Bioresistance.LOGGER.info("注册状态效果");
    }
} 