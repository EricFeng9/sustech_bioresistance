package sustech.bioresistance.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.fabricmc.fabric.api.registry.FuelRegistry;

public class KelpBucketItem extends Item {
    public KelpBucketItem(Settings settings) {
        super(settings);
        // 在构造函数中注册燃烧时间
        FuelRegistry.INSTANCE.add(this, 200);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        // 燃烧后返回空桶
        return new ItemStack(Items.BUCKET);
    }
} 