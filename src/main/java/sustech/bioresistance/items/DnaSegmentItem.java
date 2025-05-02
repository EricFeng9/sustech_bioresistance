package sustech.bioresistance.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * DNA片段物品类
 * 用于显示T6SS系统各组件的描述
 */
public class DnaSegmentItem extends Item {
    private final String descriptionKey;

    public DnaSegmentItem(Settings settings, String descriptionKey) {
        super(settings);
        this.descriptionKey = descriptionKey;
    }

    /**
     * 添加物品提示信息
     * @param stack 物品堆
     * @param world 世界
     * @param tooltip 提示列表
     * @param context 提示上下文
     */
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        
        // 添加描述
        tooltip.add(Text.translatable(descriptionKey));
    }
} 