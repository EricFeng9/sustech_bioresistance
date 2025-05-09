package sustech.bioresistance.events;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.ModItems;
import sustech.bioresistance.ModStatusEffects;
import sustech.bioresistance.data.PlagueResistanceManager;

/**
 * 鼠疫事件处理器
 * 负责处理鼠疫相关的事件和治疗
 */
public class PlagueEventHandler {

    // 鼠疫效果持续时间 (5分钟 = 300秒 = 6000刻)
    public static final int PLAGUE_DURATION = 6000;
    
    // 获取鼠疫状态效果实例
    private static final StatusEffect PLAGUE_EFFECT = ModStatusEffects.PLAGUE;
    
    /**
     * 获取当前鼠疫耶尔森菌的耐药性
     * @return 0-1之间的耐药性数值
     */
    public static double getPlagueResistance(MinecraftServer server) {
        if (server == null) return 0.0;
        PlagueResistanceManager manager = PlagueResistanceManager.getManager(server);
        return manager.getResistance();
    }
    
    /**
     * 设置鼠疫耶尔森菌的耐药性
     * @param resistance 0-1之间的耐药性数值
     * @return 是否设置成功
     */
    public static boolean setPlagueResistance(double resistance) {
        if (resistance >= 0.0 && resistance <= 1.0) {
            try {
                // 通过命令参数传入的值，通常是在命令执行时调用
                // 此时已有CommandContext上下文，可以通过context.getSource().getServer()获取服务器
                // 所以此处设置的耐药性值将在调用点通过PlagueResistanceManager直接设置
                
                // 将值限制在0.0-1.0之间
                float safeValue = (float)Math.max(0.0, Math.min(1.0, resistance));
                Bioresistance.LOGGER.info("将鼠疫耶尔森菌耐药性设置为 {}", String.format("%.1f%%", safeValue * 100));
                return true;
            } catch (Exception e) {
                Bioresistance.LOGGER.error("设置鼠疫耶尔森菌耐药性失败: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * 注册事件处理器
     */
    public static void register() {
        // 监听使用物品的事件（例如使用链霉素或抗耐药性微生物胶囊）
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            
            // 检查是否使用了治疗物品
            if (isTreatmentItem(itemStack.getItem())) {
                // 检查玩家是否有鼠疫效果
                if (player.hasStatusEffect(PLAGUE_EFFECT)) {
                    // 如果是链霉素，需要根据耐药性计算是否可以治愈
                    if (itemStack.getItem() == ModItems.STREPTOMYCIN) {
                        // 在服务端处理
                        if (!world.isClient()) {
                            MinecraftServer server = world.getServer();
                            if (server != null) {
                                PlagueResistanceManager manager = PlagueResistanceManager.getManager(server);
                                
                                // 计算治疗成功率
                                double successRate = 1.0 - manager.getResistance();
                                
                                // 判断是否治疗成功
                                if (world.random.nextDouble() < successRate) {
                                    // 治疗成功
                                    player.removeStatusEffect(PLAGUE_EFFECT);
                                    player.sendMessage(Text.translatable("streptomycin.treatment.success"), true);
                                    
                                    Bioresistance.LOGGER.info("玩家 {} 使用链霉素治愈了鼠疫", player.getName().getString());
                                    
                                    // 每次成功治疗后，细菌耐药性略微增加(0.1%)
                                    manager.increaseResistance();
                                } else {
                                    // 治疗失败
                                    player.sendMessage(Text.translatable("streptomycin.treatment.failed"), true);
                                    
                                    // 失败后耐药性增加两次(0.2%)
                                    manager.increaseResistance();
                                    manager.increaseResistance();
                                }
                                
                                // 通知玩家当前耐药性
                                player.sendMessage(
                                    Text.translatable("streptomycin.resistance")
                                        .append(manager.getResistancePercentage()), 
                                    false
                                );
                            }
                        } else {
                            // 客户端只显示提示
                            player.sendMessage(Text.translatable("streptomycin.resistance.client"), true);
                        }
                        
                        // 消耗物品（如果不是在创造模式）
                        if (!player.isCreative()) {
                            itemStack.decrement(1);
                        }
                    }
                    // 如果是抗耐药性微生物胶囊，总是成功
                    else if (itemStack.getItem() == ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES) {
                        player.removeStatusEffect(PLAGUE_EFFECT);
                        player.sendMessage(Text.translatable("plague.cured.by.capsules"), true);
                        
                        Bioresistance.LOGGER.info("玩家 {} 使用抗耐药性微生物胶囊治愈了鼠疫", player.getName().getString());
                        
                        // 消耗物品（如果不是在创造模式）
                        if (!player.isCreative()) {
                            itemStack.decrement(1);
                        }
                    }
                }
            }
            
            // 不改变原有行为
            return TypedActionResult.pass(itemStack);
        });
        
        Bioresistance.LOGGER.info("注册鼠疫事件处理器");
    }
    
    /**
     * 检查物品是否为治疗物品
     * @param item 待检查的物品
     * @return 是否为治疗物品
     */
    private static boolean isTreatmentItem(Item item) {
        // 检查是否为链霉素或抗耐药性微生物胶囊
        return item == ModItems.STREPTOMYCIN || 
               item == ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES;
    }
    
    /**
     * 给实体应用鼠疫效果
     * @param entity 目标实体
     */
    public static void applyPlagueEffect(LivingEntity entity) {
        // 添加鼠疫效果
        entity.addStatusEffect(new StatusEffectInstance(
            PLAGUE_EFFECT, // 鼠疫效果
            PLAGUE_DURATION,         // 持续时间 5分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            false                    // 不可以被普通方式治愈
        ));
        
        // 添加虚弱效果
        entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.WEAKNESS,  // 虚弱效果
            PLAGUE_DURATION,         // 持续时间 5分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
        
        // 添加缓慢效果
        entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS,  // 缓慢效果
            PLAGUE_DURATION,         // 持续时间 5分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
        
        // 如果是玩家，发送消息
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("plague.infected"), true);
        }
    }
} 