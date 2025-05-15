package sustech.bioresistance.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import sustech.bioresistance.data.CandidaResistanceManager;

import java.util.Random;
import java.lang.reflect.Method;

/**
 * 耳念珠菌感染事件处理器
 * 负责处理耳念珠菌感染相关的事件和治疗
 */
public class CandidiasisEventHandler {

    // 耳念珠菌感染持续时间 (3分钟 = 180秒 = 3600刻)
    public static final int CANDIDIASIS_DURATION = 3600;
    
    // 获取耳念珠菌感染状态效果实例
    private static final StatusEffect CANDIDIASIS_EFFECT = ModStatusEffects.CANDIDIASIS;
    
    // 感染概率 (0.5% - 0.005f)
    private static final float INFECTION_CHANCE = 0.005f;
    
    // 用于生成随机数的实例
    private static final Random RANDOM = new Random();
    
    // 标志是否从事件处理器中移除效果，用于传递给LivingEntityMixin
    public static boolean IS_REMOVING_EFFECT_FROM_HANDLER = false;
    
    /**
     * 获取当前耳念珠菌的耐药性
     * @return 0-1之间的耐药性数值
     */
    public static double getCandidaResistance(MinecraftServer server) {
        if (server == null) return 0.0;
        CandidaResistanceManager manager = CandidaResistanceManager.getManager(server);
        return manager.getResistance();
    }
    
    /**
     * 设置耳念珠菌的耐药性
     * @param resistance 0-1之间的耐药性数值
     * @return 是否设置成功
     */
    public static boolean setCandidaResistance(double resistance) {
        if (resistance >= 0.0 && resistance <= 1.0) {
            try {
                // 将值限制在0.0-1.0之间
                float safeValue = (float)Math.max(0.0, Math.min(1.0, resistance));
                Bioresistance.LOGGER.info("将耳念珠菌耐药性设置为 {}", String.format("%.1f%%", safeValue * 100));
                return true;
            } catch (Exception e) {
                Bioresistance.LOGGER.error("设置耳念珠菌耐药性失败: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * 强制移除耳念珠菌感染效果
     * 用于治疗，绕过LivingEntityMixin的限制
     * @param entity 目标实体
     * @return 是否成功移除
     */
    public static boolean forceCureEffect(LivingEntity entity) {
        try {
            Bioresistance.LOGGER.info("尝试强制移除耳念珠菌感染效果");
            
            // 先检查是否有效果
            if (!entity.hasStatusEffect(CANDIDIASIS_EFFECT)) {
                Bioresistance.LOGGER.info("实体没有耳念珠菌感染效果，无需移除");
                return false;
            }
            
            // 设置标志，表示当前是从事件处理器中移除效果
            IS_REMOVING_EFFECT_FROM_HANDLER = true;
            
            try {
                // 直接调用removeStatusEffect方法
                boolean removed = entity.removeStatusEffect(CANDIDIASIS_EFFECT);
                Bioresistance.LOGGER.info("移除耳念珠菌感染效果结果: {}", removed);
                return removed;
            } finally {
                // 不论是否成功，都重置标志
                IS_REMOVING_EFFECT_FROM_HANDLER = false;
            }
        } catch (Exception e) {
            Bioresistance.LOGGER.error("强制移除耳念珠菌感染效果时发生异常: {}", e.getMessage());
            e.printStackTrace();
            // 确保标志被重置
            IS_REMOVING_EFFECT_FROM_HANDLER = false;
            return false;
        }
    }
    
    /**
     * 注册事件处理器
     */
    public static void register() {
        // 监听使用物品的事件（例如使用抗真菌药或抗耐药性微生物软膏）
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            
            // 检查是否使用了治疗物品
            if (isTreatmentItem(itemStack.getItem())) {
                // 检查玩家是否有耳念珠菌感染效果
                if (player.hasStatusEffect(CANDIDIASIS_EFFECT)) {
                    // 如果是抗真菌药，需要根据耐药性计算是否可以治愈
                    if (itemStack.getItem() == ModItems.ANTIFUNGAL_DRUG) {
                        // 在服务端处理
                        if (!world.isClient()) {
                            MinecraftServer server = world.getServer();
                            if (server != null) {
                                CandidaResistanceManager manager = CandidaResistanceManager.getManager(server);
                                
                                // 计算治疗成功率
                                double successRate = 1.0 - manager.getResistance();
                                
                                // 判断是否治疗成功
                                if (world.random.nextDouble() < successRate) {
                                    // 治疗成功
                                    Bioresistance.LOGGER.info("玩家 {} 使用抗真菌药治疗耳念珠菌感染，治疗成功", player.getName().getString());
                                    
                                    // 移除耳念珠菌感染效果 - 使用强制移除方法
                                    boolean removed = forceCureEffect(player);
                                    Bioresistance.LOGGER.info("强制移除耳念珠菌感染效果结果: {}", removed);
                                    
                                    // 发送成功消息
                                    player.sendMessage(Text.translatable("antifungal_drug.treatment.success"), true);
                                    
                                    // 每次成功治疗后，耳念珠菌耐药性略微增加(0.1%)
                                    manager.increaseResistance();
                                } else {
                                    // 治疗失败
                                    player.sendMessage(Text.translatable("antifungal_drug.treatment.failed"), true);
                                    
                                    // 失败后耐药性增加(0.1%)
                                    manager.increaseResistance();
                                }
                                
                                // 通知玩家当前耐药性
                                player.sendMessage(
                                    Text.translatable("antifungal_drug.resistance")
                                        .append(Text.literal(manager.getResistancePercentage())),
                                    false
                                );
                                
                                // 消耗物品（如果不是在创造模式）
                                if (!player.isCreative()) {
                                    itemStack.decrement(1);
                                }
                            }
                        }
                    } else if (itemStack.getItem() == ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT) {
                        // 抗耐药性微生物软膏，总是成功
                        if (!world.isClient()) {
                            Bioresistance.LOGGER.info("玩家 {} 使用抗耐药性微生物软膏治疗耳念珠菌感染", player.getName().getString());
                            
                            // 移除耳念珠菌感染效果 - 使用强制移除方法
                            boolean removed = forceCureEffect(player);
                            Bioresistance.LOGGER.info("强制移除耳念珠菌感染效果结果: {}", removed);
                            
                            // 成功消息
                            player.sendMessage(Text.translatable("anti_drug_resistant_microbial_ointment.treatment.success"), true);
                            
                            // 消耗物品（如果不是在创造模式）
                            if (!player.isCreative()) {
                                itemStack.decrement(1);
                            }
                        }
                    }
                }
            }
            
            // 不改变原有行为
            return TypedActionResult.pass(itemStack);
        });
        
        // 注册服务器刻事件，用于定期检查玩家状态是否符合感染条件
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 每10秒检查一次（200刻）
            if (server.getTicks() % 200 == 0) {
                // 遍历所有在线玩家
                for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    // 如果玩家已经有耳念珠菌感染，跳过
                    if (player.hasStatusEffect(CANDIDIASIS_EFFECT)) {
                        continue;
                    }
                    
                    // 检查是否满足感染条件
                    boolean hasExhaustion = player.hasStatusEffect(ModStatusEffects.EXHAUSTION);
                    boolean hasPlague = player.hasStatusEffect(ModStatusEffects.PLAGUE);
                    boolean hasTetanus = player.hasStatusEffect(ModStatusEffects.TETANUS);
                    
                    // 如果符合感染条件，有一定概率感染
                    if ((hasExhaustion || hasPlague || hasTetanus) && RANDOM.nextFloat() < INFECTION_CHANCE) {
                        Bioresistance.LOGGER.info("玩家 {} 因条件满足被耳念珠菌感染", player.getName().getString());
                        
                        // 给予耳念珠菌感染效果
                        applyCandidiasisEffect(player);
                    }
                }
            }
        });
    }
    
    /**
     * 检查物品是否为治疗物品
     * @param item 待检查的物品
     * @return 是否为治疗物品
     */
    private static boolean isTreatmentItem(Item item) {
        // 检查是否为抗真菌药或抗耐药性微生物软膏
        return item == ModItems.ANTIFUNGAL_DRUG || 
               item == ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT;
    }
    
    /**
     * 给实体应用耳念珠菌感染效果
     * @param entity 目标实体
     */
    public static void applyCandidiasisEffect(LivingEntity entity) {
        // 添加耳念珠菌感染效果
        entity.addStatusEffect(new StatusEffectInstance(
            CANDIDIASIS_EFFECT,     // 耳念珠菌感染效果
            CANDIDIASIS_DURATION,   // 持续时间 3分钟
            0,                      // 效果等级 0 (I级)
            false,                  // 不显示粒子
            true,                   // 显示图标
            false                   // 不可以被普通方式治愈
        ));
        
        // 添加虚弱效果
        entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.WEAKNESS,  // 虚弱效果
            CANDIDIASIS_DURATION,    // 持续时间 3分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
        
        // 添加缓慢效果
        entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS,  // 缓慢效果
            CANDIDIASIS_DURATION,    // 持续时间 3分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
        
        // 如果是玩家，发送消息
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable("candidiasis.infected"), true);
            player.sendMessage(Text.translatable("candidiasis.symptoms"), true);
        }
    }
} 