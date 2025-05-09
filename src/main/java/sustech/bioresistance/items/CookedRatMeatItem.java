package sustech.bioresistance.items;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

public class CookedRatMeatItem extends Item {
    public CookedRatMeatItem(Settings settings) {
        super(settings.food(new FoodComponent.Builder()
                .hunger(5)
                .saturationModifier(0.6f)
                .meat()
                .build()
        ));
    }
} 