package sustech.bioresistance.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import sustech.bioresistance.data.PlagueResistanceManager;

/**
 * 链霉素物品
 * 用于治疗鼠疫，但存在耐药性机制
 */
public class StreptomycinItem extends Item {

    public StreptomycinItem(Settings settings) {
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
        tooltip.add(Text.translatable("streptomycin.description"));
        
        // 添加耐药性信息
        if (world != null && !world.isClient()) {
            // 服务端直接获取数据
            MinecraftServer server = world.getServer();
            if (server != null) {
                PlagueResistanceManager manager = PlagueResistanceManager.getManager(server);
                tooltip.add(
                    Text.translatable("streptomycin.resistance")
                        .append(Text.literal(String.format("%.1f%%", manager.getResistance() * 100)).formatted(Formatting.RED))
                );
            }
        } else {
            // 客户端使用缓存的耐药性数据
            tooltip.add(
                Text.translatable("streptomycin.resistance")
                    .append(Text.literal(PlagueResistanceManager.getCachedResistancePercentage()).formatted(Formatting.RED))
            );
        }
    }
    
    /**
     * 使用后增加耐药性
     * 每次使用增加0.1%的耐药性
     */
    public static void increaseResistance(World world) {
        if (world.isClient()) return;
        
        MinecraftServer server = world.getServer();
        if (server != null) {
            PlagueResistanceManager manager = PlagueResistanceManager.getManager(server);
            float currentResistance = manager.getResistance();
            // 每次使用增加0.001的耐药性(相当于0.1%)
            manager.setResistance(currentResistance + 0.001f);
        }
    }
} 