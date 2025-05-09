package sustech.bioresistance;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

public class RawRatMeatItem extends Item {
    public RawRatMeatItem(Settings settings) {
        super(settings.food(new FoodComponent.Builder()
                .hunger(2)
                .saturationModifier(0.3f)
                .meat()
                .statusEffect(new StatusEffectInstance(StatusEffectsRegistry.PLAGUE, 600, 0), 0.3f) // 30%几率感染
                .build()
        ));
    }
}
