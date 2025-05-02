package sustech.bioresistance.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import sustech.bioresistance.data.CandidaResistanceManager;

/**
 * 抗真菌药物品
 * 用于治疗耳念珠菌感染，但存在耐药性机制
 */
public class AntifungalDrugItem extends Item {

    public AntifungalDrugItem(Settings settings) {
        super(settings);
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
        
        // 添加第一行描述
        tooltip.add(Text.translatable("antifungal_drug.description"));
        
        // 添加耐药性信息
        if (world != null && !world.isClient()) {
            // 服务端直接获取数据
            MinecraftServer server = world.getServer();
            if (server != null) {
                CandidaResistanceManager manager = CandidaResistanceManager.getManager(server);
                tooltip.add(
                    Text.translatable("antifungal_drug.resistance")
                        .append(Text.literal(manager.getResistancePercentage()).formatted(Formatting.RED))
                );
            }
        } else {
            // 客户端使用缓存的耐药性数据
            tooltip.add(
                Text.translatable("antifungal_drug.resistance")
                    .append(Text.literal(CandidaResistanceManager.getCachedResistancePercentage()).formatted(Formatting.RED))
            );
        }
    }
} 