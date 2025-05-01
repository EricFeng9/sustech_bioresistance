package sustech.bioresistance.events;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.ModItems;
import sustech.bioresistance.ModStatusEffects;
import net.minecraft.text.Text;

/**
 * 破伤风事件处理器
 * 负责监听玩家使用/攻击带有铁制工具的事件，并给予破伤风效果
 */
public class TetanusEventHandler {

    // 破伤风效果持续时间 (5分钟 = 300秒 = 6000刻)
    private static final int TETANUS_DURATION = 600;
    
    /**
     * 注册事件处理器
     */
    public static void register() {
        // 监听使用物品的事件（例如使用铁锄等）
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            
            // 检查是否为铁制工具
            if (isIronTool(itemStack.getItem()) && !world.isClient()) {
                // 有0.1% - 0.001f的几率感染破伤风
                if (world.random.nextFloat() < 1f) {
                    Bioresistance.LOGGER.info("玩家 {} 使用铁制工具感染了破伤风", player.getName().getString());
                    
                    // 给予破伤风效果
                    applyTetanusEffect(player);
                }
            }
            
            // 检查是否使用了治疗物品
            if (isTreatmentItem(itemStack.getItem()) && !world.isClient()) {
                // 如果玩家有破伤风效果
                if (player.hasStatusEffect(ModStatusEffects.TETANUS)) {
                    // 区分不同的治疗物品
                    if (itemStack.getItem() == ModItems.METRONIDAZOLE) {
                        // 获取耐药性管理器
                        sustech.bioresistance.data.TetanusResistanceManager resistanceManager = 
                            sustech.bioresistance.data.TetanusResistanceManager.getManager(player.getServer());
                        
                        // 每次使用甲硝唑，增加耐药性
                        resistanceManager.increaseResistance();
                        
                        // 根据当前耐药性判断治疗是否会失败
                        if (resistanceManager.willTreatmentFail()) {
                            // 治疗失败
                            Bioresistance.LOGGER.info("玩家 {} 使用甲硝唑治疗破伤风失败，当前耐药性: {}", 
                                player.getName().getString(), resistanceManager.getResistancePercentage());
                            
                            // 发送失败消息给玩家
                            player.sendMessage(Text.translatable("metronidazole.treatment.failed"), false);
                            
                            // 消耗物品（如果不是在创造模式）
                            if (!player.isCreative()) {
                                itemStack.decrement(1);
                            }
                        } else {
                            // 治疗成功，移除破伤风效果
                            player.removeStatusEffect(ModStatusEffects.TETANUS);
                            
                            // 如果有凋零效果也一并移除
                            if (player.hasStatusEffect(StatusEffects.WITHER)) {
                                player.removeStatusEffect(StatusEffects.WITHER);
                            }
                            
                            Bioresistance.LOGGER.info("玩家 {} 使用甲硝唑成功治愈了破伤风", player.getName().getString());
                            
                            // 发送成功消息给玩家
                            player.sendMessage(Text.translatable("metronidazole.treatment.success"), false);
                            
                            // 消耗物品（如果不是在创造模式）
                            if (!player.isCreative()) {
                                itemStack.decrement(1);
                            }
                        }
                    } else if (itemStack.getItem() == ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES) {
                        // 抗耐药性微生物胶囊，总是成功
                        player.removeStatusEffect(ModStatusEffects.TETANUS);
                        
                        // 如果有凋零效果也一并移除
                        if (player.hasStatusEffect(StatusEffects.WITHER)) {
                            player.removeStatusEffect(StatusEffects.WITHER);
                        }
                        
                        Bioresistance.LOGGER.info("玩家 {} 使用抗耐药性微生物胶囊治愈了破伤风", player.getName().getString());
                        
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
        
        // 监听攻击方块的事件（例如使用铁镐挖掘）
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack itemStack = player.getStackInHand(hand);
            
            // 检查是否为铁制工具
            if (isIronTool(itemStack.getItem()) && !world.isClient()) {
                // 有0.1% - 0.001f的几率感染破伤风
                if (world.random.nextFloat() < 1f) {
                    Bioresistance.LOGGER.info("玩家 {} 使用铁制工具感染了破伤风", player.getName().getString());
                    
                    // 给予破伤风效果
                    applyTetanusEffect(player);
                }
            }
            
            // 不改变原有行为
            return ActionResult.PASS;
        });
        
        // 监听玩家受伤的事件
        PlayerHurtCallback.EVENT.register((entity, source, amount) -> {
            // 检查是否为客户端，跳过客户端处理
            if (entity.getWorld().isClient()) {
                return ActionResult.PASS;
            }
            
            // 玩家被铁傀儡攻击时百分百感染破伤风
            if (isIronGolemDamage(source)) {
                if (entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;
                    Bioresistance.LOGGER.info("玩家 {} 被铁傀儡攻击感染了破伤风", player.getName().getString());
                    applyTetanusEffect(player);
                } else if (entity instanceof LivingEntity) {
                    // 如果是其他生物被铁傀儡攻击，也给予破伤风效果
                    LivingEntity livingEntity = (LivingEntity) entity;
                    Bioresistance.LOGGER.info("生物被铁傀儡攻击感染了破伤风");
                    applyTetanusEffectToMob(livingEntity);
                }
                return ActionResult.PASS;
            }
            
            // 处理玩家实体
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                // 检查伤害源是否来自铁制工具或弓箭
                if (isIronWeaponDamage(source) || isArrowDamage(source)) {
                    // 有100%的几率感染破伤风
                    if (player.getWorld().random.nextFloat() < 1f) {
                        Bioresistance.LOGGER.info("玩家 {} 被铁器或弓箭攻击感染了破伤风", player.getName().getString());
                        
                        // 给予破伤风效果
                        applyTetanusEffect(player);
                    }
                }
            }
            
            // 不改变原有行为
            return ActionResult.PASS;
        });
        
        Bioresistance.LOGGER.info("注册破伤风事件处理器");
    }
    
    /**
     * 检查伤害源是否来自铁制武器
     * @param source 伤害源
     * @return 是否为铁制武器伤害
     */
    private static boolean isIronWeaponDamage(DamageSource source) {
        if (source.getAttacker() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getAttacker();
            ItemStack weapon = attacker.getMainHandStack();
            return isIronTool(weapon.getItem());
        }
        return false;
    }
    
    /**
     * 检查伤害源是否来自弓箭
     * @param source 伤害源
     * @return 是否为弓箭伤害
     */
    private static boolean isArrowDamage(DamageSource source) {
        return source.getSource() instanceof ArrowEntity;
    }
    
    /**
     * 检查伤害源是否来自铁傀儡
     * @param source 伤害源
     * @return 是否为铁傀儡伤害
     */
    private static boolean isIronGolemDamage(DamageSource source) {
        // 检查攻击者是否存在
        if (source.getAttacker() == null) {
            return false;
        }
        
        // 检查实体名称包含"iron_golem"，支持多种语言环境
        String entityName = source.getAttacker().getName().getString().toLowerCase();
        return entityName.contains("iron") && entityName.contains("golem");
    }
    
    /**
     * 检查物品是否为铁制工具
     * @param item 待检查的物品
     * @return 是否为铁制工具
     */
    private static boolean isIronTool(Item item) {
        // 检查是否为铁制工具（铁剑、铁斧、铁镐、铁锄）
        return item == Items.IRON_SWORD || 
               item == Items.IRON_AXE || 
               item == Items.IRON_PICKAXE || 
               item == Items.IRON_HOE || 
               item == Items.IRON_SHOVEL;
    }
    
    /**
     * 检查物品是否为治疗物品
     * @param item 待检查的物品
     * @return 是否为治疗物品
     */
    private static boolean isTreatmentItem(Item item) {
        // 检查是否为甲硝唑或抗耐药性微生物胶囊
        return item == ModItems.METRONIDAZOLE || 
               item == ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES;
    }
    
    /**
     * 给玩家应用破伤风效果
     * @param player 目标玩家
     */
    private static void applyTetanusEffect(net.minecraft.entity.player.PlayerEntity player) {
        // 添加破伤风效果
        player.addStatusEffect(new StatusEffectInstance(
            ModStatusEffects.TETANUS, // 破伤风效果
            TETANUS_DURATION,         // 持续时间 5分钟
            0,                        // 效果等级 0 (I级)
            false,                    // 不显示粒子
            true,                     // 显示图标
            false                     // 不可以被普通方式治愈，只能通过我们特定的物品
        ));
        
        // 添加虚弱效果
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.WEAKNESS,  // 虚弱效果
            TETANUS_DURATION,        // 持续时间 5分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
    }
    
    /**
     * 给生物应用破伤风效果
     * @param entity 目标生物
     */
    private static void applyTetanusEffectToMob(LivingEntity entity) {
        // 添加虚弱效果
        entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.WEAKNESS,  // 虚弱效果
            TETANUS_DURATION,        // 持续时间 5分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
        
        // 添加缓慢效果模拟"破伤风"
        entity.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS,  // 缓慢效果
            TETANUS_DURATION,        // 持续时间 5分钟
            0,                       // 效果等级 0 (I级)
            false,                   // 不显示粒子
            true,                    // 显示图标
            true                     // 可以被治愈
        ));
    }
} 