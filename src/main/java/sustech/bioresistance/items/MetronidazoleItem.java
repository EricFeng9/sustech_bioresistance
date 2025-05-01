package sustech.bioresistance.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sustech.bioresistance.data.TetanusResistanceManager;

import java.util.List;

/**
 * 甲硝唑物品
 * 用于治疗破伤风，但存在耐药性机制
 */
public class MetronidazoleItem extends Item {

    public MetronidazoleItem(Settings settings) {
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
        tooltip.add(Text.translatable("metronidazole.description"));
        
        // 添加耐药性信息
        if (world != null && !world.isClient()) {
            // 服务端直接获取数据
            MinecraftServer server = world.getServer();
            if (server != null) {
                TetanusResistanceManager manager = TetanusResistanceManager.getManager(server);
                tooltip.add(
                    Text.translatable("metronidazole.resistance")
                        .append(Text.literal(manager.getResistancePercentage()).formatted(Formatting.RED))
                );
            }
        } else {
            // 客户端使用缓存的耐药性数据
            tooltip.add(
                Text.translatable("metronidazole.resistance")
                    .append(Text.literal(TetanusResistanceManager.getCachedResistancePercentage()).formatted(Formatting.RED))
            );
        }
    }
} 