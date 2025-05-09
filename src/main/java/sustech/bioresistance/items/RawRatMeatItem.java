package sustech.bioresistance.items;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import sustech.bioresistance.ModStatusEffects;
import sustech.bioresistance.events.PlagueEventHandler;

public class RawRatMeatItem extends Item {
    
    // 创建一个静态的StatusEffect引用，避免每次创建物品时进行类型转换
    private static final StatusEffect PLAGUE_EFFECT = (StatusEffect)ModStatusEffects.PLAGUE;
    
    public RawRatMeatItem(Settings settings) {
        super(settings.food(new FoodComponent.Builder()
                .hunger(2)
                .saturationModifier(0.3f)
                .meat()
                .statusEffect(new StatusEffectInstance(PLAGUE_EFFECT, PlagueEventHandler.PLAGUE_DURATION, 0), 0.01f) // 1%几率感染鼠疫，持续5分钟
                .build()
        ));
    }
}
