package sustech.bioresistance.commands;

import java.util.HashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.Command;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.ModEntities;
import sustech.bioresistance.ModStatusEffects;
import sustech.bioresistance.data.CandidaResistanceManager;
import sustech.bioresistance.entities.RatEntity;
import sustech.bioresistance.events.CandidiasisEventHandler;
import sustech.bioresistance.events.PlagueEventHandler;
import sustech.bioresistance.events.TetanusEventHandler;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * 生物抗性模组命令系统
 */
public class BioresistanceCommands {

    /**
     * 注册所有模组命令
     * @param dispatcher 命令分发器
     * @param registryAccess 命令注册访问器
     * @param environment 环境类型
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, 
                                CommandRegistryAccess registryAccess, 
                                CommandManager.RegistrationEnvironment environment) {
        
        // 注册主命令 /bioresistance
        LiteralArgumentBuilder<ServerCommandSource> bioresistanceCommand = net.minecraft.server.command.CommandManager.literal("bioresistance");
        
        // 帮助子命令
        bioresistanceCommand.then(CommandManager.literal("help")
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                String langKey = "zh"; // 默认中文
                
                // 发送命令列表
                source.sendFeedback(() -> Text.translatable("commands.bioresistance.title." + langKey), false);
                source.sendFeedback(() -> Text.literal("/bioresistance help - ")
                    .append(Text.translatable("commands.bioresistance.help.description." + langKey)), false);
                source.sendFeedback(() -> Text.literal("/bioresistance tetanus_resistance set <value> - ")
                    .append(Text.translatable("commands.bioresistance.tetanus_resistance.set.description." + langKey)), false);
                source.sendFeedback(() -> Text.literal("/bioresistance plague_resistance set <value> - ")
                    .append(Text.translatable("commands.bioresistance.plague_resistance.set.description." + langKey)), false);
                source.sendFeedback(() -> Text.literal("/bioresistance candida_resistance set <value> - ")
                    .append(Text.translatable("commands.bioresistance.candida_resistance.set.description." + langKey)), false);
                source.sendFeedback(() -> Text.literal("/bioresistance summon_rat - ")
                    .append(Text.translatable("commands.bioresistance.summon_rat.description." + langKey)), false);
                source.sendFeedback(() -> Text.literal("/bioresistance apply_effect <effect> [player] - ")
                    .append(Text.translatable("commands.bioresistance.apply_effect.description." + langKey)), false);
                source.sendFeedback(() -> Text.literal("/bioresistance check_effects [player] - ")
                    .append(Text.literal("检查玩家的疾病状态")), false);
                
                return 1;
            }));
        
        // 破伤风耐药性命令
        bioresistanceCommand.then(CommandManager.literal("tetanus_resistance")
            .then(CommandManager.literal("set")
                .requires(source -> source.hasPermissionLevel(2)) // 只有OP才能使用
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        double value = DoubleArgumentType.getDouble(context, "value");
                        String langKey = "zh"; // 默认中文
                        
                        // 设置破伤风耐药性
                        if (TetanusEventHandler.setTetanusResistance(value)) {
                            source.sendFeedback(() -> Text.translatable(
                                "commands.bioresistance.tetanus_resistance.set.success." + langKey, 
                                String.format("%.1f%%", value * 100)
                            ), true);
                            
                            // 直接通过服务器实例更新耐药性数据
                            MinecraftServer server = source.getServer();
                            if (server != null) {
                                sustech.bioresistance.data.TetanusResistanceManager manager = 
                                    sustech.bioresistance.data.TetanusResistanceManager.getManager(server);
                                manager.setResistance((float)value);
                            }
                            
                            return 1;
                        } else {
                            source.sendError(Text.translatable("commands.bioresistance.tetanus_resistance.set.error." + langKey));
                            return 0;
                        }
                    }))));
        
        // 鼠疫耐药性命令
        bioresistanceCommand.then(CommandManager.literal("plague_resistance")
            .then(CommandManager.literal("set")
                .requires(source -> source.hasPermissionLevel(2)) // 只有OP才能使用
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        double value = DoubleArgumentType.getDouble(context, "value");
                        String langKey = "zh"; // 默认中文
                        
                        // 设置鼠疫耐药性
                        if (PlagueEventHandler.setPlagueResistance(value)) {
                            source.sendFeedback(() -> Text.translatable(
                                "commands.bioresistance.plague_resistance.set.success." + langKey, 
                                String.format("%.1f%%", value * 100)
                            ), true);
                            
                            // 直接通过服务器实例更新耐药性数据
                            MinecraftServer server = source.getServer();
                            if (server != null) {
                                sustech.bioresistance.data.PlagueResistanceManager manager = 
                                    sustech.bioresistance.data.PlagueResistanceManager.getManager(server);
                                manager.setResistance((float)value);
                            }
                            
                            return 1;
                        } else {
                            source.sendError(Text.translatable("commands.bioresistance.plague_resistance.set.error." + langKey));
                            return 0;
                        }
                    }))));
                    
        // 耳念珠菌耐药性命令
        bioresistanceCommand.then(CommandManager.literal("candida_resistance")
            .then(CommandManager.literal("set")
                .requires(source -> source.hasPermissionLevel(2)) // 只有OP才能使用
                .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        double value = DoubleArgumentType.getDouble(context, "value");
                        String langKey = "zh"; // 默认中文
                        
                        // 设置耳念珠菌耐药性
                        if (CandidiasisEventHandler.setCandidaResistance(value)) {
                            source.sendFeedback(() -> Text.translatable(
                                "commands.bioresistance.candida_resistance.set.success." + langKey, 
                                String.format("%.1f%%", value * 100)
                            ), true);
                            
                            // 直接通过服务器实例更新耐药性数据
                            MinecraftServer server = source.getServer();
                            if (server != null) {
                                sustech.bioresistance.data.CandidaResistanceManager manager = 
                                    sustech.bioresistance.data.CandidaResistanceManager.getManager(server);
                                manager.setResistance((float)value);
                            }
                            
                            return 1;
                        } else {
                            source.sendError(Text.translatable("commands.bioresistance.candida_resistance.set.error." + langKey));
                            return 0;
                        }
                    }))));
                    
        // 生成老鼠命令
        bioresistanceCommand.then(CommandManager.literal("summon_rat")
            .requires(source -> source.hasPermissionLevel(2)) // 只有OP才能使用
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                PlayerEntity player = source.getPlayer();
                
                // 确保命令由玩家执行
                if (player == null) {
                    source.sendError(Text.literal("该命令只能由玩家执行"));
                    return 0;
                }
                
                // 在玩家位置生成老鼠
                BlockPos pos = player.getBlockPos();
                RatEntity rat = new RatEntity(ModEntities.RAT, player.getWorld());
                rat.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                player.getWorld().spawnEntity(rat);
                
                source.sendFeedback(() -> Text.literal("已在玩家位置生成老鼠"), true);
                return 1;
            }));
            
        // 疾病检查命令
        bioresistanceCommand.then(CommandManager.literal("check_effects")
            .requires(source -> source.hasPermissionLevel(2)) // 只有OP才能使用
            .executes(context -> {
                ServerCommandSource source = context.getSource();
                PlayerEntity player = source.getPlayer();
                
                if (player == null) {
                    source.sendError(Text.literal("该命令只能由玩家执行"));
                    return 0;
                }
                
                // 检查玩家当前疾病状态
                return checkPlayerDiseases(source, player);
            })
            .then(CommandManager.argument("player", EntityArgumentType.player())
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                    
                    // 检查指定玩家的疾病状态
                    return checkPlayerDiseases(source, player);
                })));
            
        // 强制感染命令
        bioresistanceCommand.then(CommandManager.literal("apply_effect")
            .requires(source -> source.hasPermissionLevel(2)) // 只有OP才能使用
            .then(CommandManager.literal("all")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    
                    if (player == null) {
                        source.sendError(Text.literal("该命令只能由玩家执行"));
                        return 0;
                    }
                    
                    // 应用所有疾病效果
                    PlagueEventHandler.applyPlagueEffect(player);
                    player.addStatusEffect(new StatusEffectInstance(
                        (StatusEffect)ModStatusEffects.TETANUS, 
                        6000,  // 5分钟
                        0, 
                        false, 
                        true, 
                        false
                    ));
                    player.addStatusEffect(new StatusEffectInstance(
                        (StatusEffect)ModStatusEffects.EXHAUSTION, 
                        6000,  // 5分钟
                        0, 
                        false, 
                        true, 
                        false
                    ));
                    
                    source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用所有疾病效果"), true);
                    return 1;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        
                        // 应用所有疾病效果
                        PlagueEventHandler.applyPlagueEffect(player);
                        player.addStatusEffect(new StatusEffectInstance(
                            (StatusEffect)ModStatusEffects.TETANUS, 
                            6000,  // 5分钟
                            0, 
                            false, 
                            true, 
                            false
                        ));
                        player.addStatusEffect(new StatusEffectInstance(
                            (StatusEffect)ModStatusEffects.EXHAUSTION, 
                            6000,  // 5分钟
                            0, 
                            false, 
                            true, 
                            false
                        ));
                        
                        source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用所有疾病效果"), true);
                        return 1;
                    })))
            .then(CommandManager.literal("plague")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    
                    if (player == null) {
                        source.sendError(Text.literal("该命令只能由玩家执行"));
                        return 0;
                    }
                    
                    // 应用鼠疫效果
                    PlagueEventHandler.applyPlagueEffect(player);
                    
                    source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用鼠疫效果"), true);
                    return 1;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        
                        // 应用鼠疫效果
                        PlagueEventHandler.applyPlagueEffect(player);
                        
                        source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用鼠疫效果"), true);
                        return 1;
                    })))
            .then(CommandManager.literal("tetanus")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    
                    if (player == null) {
                        source.sendError(Text.literal("该命令只能由玩家执行"));
                        return 0;
                    }
                    
                    // 应用破伤风效果
                    player.addStatusEffect(new StatusEffectInstance(
                        (StatusEffect)ModStatusEffects.TETANUS, 
                        6000,  // 5分钟
                        0, 
                        false, 
                        true, 
                        false
                    ));
                    
                    source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用破伤风效果"), true);
                    return 1;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        
                        // 应用破伤风效果
                        player.addStatusEffect(new StatusEffectInstance(
                            (StatusEffect)ModStatusEffects.TETANUS, 
                            6000,  // 5分钟
                            0, 
                            false, 
                            true, 
                            false
                        ));
                        
                        source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用破伤风效果"), true);
                        return 1;
                    })))
            .then(CommandManager.literal("exhaustion")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    
                    if (player == null) {
                        source.sendError(Text.literal("该命令只能由玩家执行"));
                        return 0;
                    }
                    
                    // 应用过度劳累效果
                    player.addStatusEffect(new StatusEffectInstance(
                        (StatusEffect)ModStatusEffects.EXHAUSTION, 
                        6000,  // 5分钟
                        0, 
                        false, 
                        true, 
                        false
                    ));
                    
                    source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用过度劳累效果"), true);
                    return 1;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        
                        // 应用过度劳累效果
                        player.addStatusEffect(new StatusEffectInstance(
                            (StatusEffect)ModStatusEffects.EXHAUSTION, 
                            6000,  // 5分钟
                            0, 
                            false, 
                            true, 
                            false
                        ));
                        
                        source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用过度劳累效果"), true);
                        return 1;
                    })))
            .then(CommandManager.literal("candidiasis")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    
                    if (player == null) {
                        source.sendError(Text.literal("该命令只能由玩家执行"));
                        return 0;
                    }
                    
                    // 应用耳念珠菌感染效果
                    CandidiasisEventHandler.applyCandidiasisEffect(player);
                    
                    source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用耳念珠菌感染效果"), true);
                    return 1;
                })
                .then(CommandManager.argument("player", EntityArgumentType.player())
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        PlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        
                        // 应用耳念珠菌感染效果
                        CandidiasisEventHandler.applyCandidiasisEffect(player);
                        
                        source.sendFeedback(() -> Text.literal("已对玩家 " + player.getName().getString() + " 应用耳念珠菌感染效果"), true);
                        return 1;
                    })))
        );

        // 添加生成诊所的命令
        dispatcher.register(
            CommandManager.literal("spawnClinic")
                .requires(source -> source.hasPermissionLevel(2)) // 需要管理员权限
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = source.getPlayer();
                    
                    if (player != null) {
                        BlockPos pos = player.getBlockPos();
                        ServerWorld world = source.getWorld();
                        
                        // 获取诊所结构ID并生成
                        Identifier structureId = new Identifier("bio-resistance", "clinic");
                        
                        // 告知玩家
                        source.sendFeedback(() -> Text.literal("尝试在当前位置生成诊所结构"), false);
                        
                        // 在以后实现自定义结构生成
                        // 目前只是发送消息作为测试
                        return Command.SINGLE_SUCCESS;
                    } else {
                        source.sendError(Text.literal("此命令只能由玩家执行"));
                        return 0;
                    }
                })
        );

        // 注册命令
        dispatcher.register(bioresistanceCommand);
        
        Bioresistance.LOGGER.info("已注册生物抗性模组命令");
    }
    
    /**
     * 获取玩家当前的所有疾病效果
     * @param player 玩家实体
     * @return 疾病效果列表及其名称的映射
     */
    private static Map<StatusEffect, String> getPlayerDiseases(PlayerEntity player) {
        Map<StatusEffect, String> diseases = new HashMap<>();
        
        // 直接检查具体的状态效果
        if (player.hasStatusEffect(ModStatusEffects.PLAGUE)) {
            diseases.put(ModStatusEffects.PLAGUE, "鼠疫");
            Bioresistance.LOGGER.info("发现鼠疫效果");
        }
        
        if (player.hasStatusEffect(ModStatusEffects.TETANUS)) {
            diseases.put(ModStatusEffects.TETANUS, "破伤风");
            Bioresistance.LOGGER.info("发现破伤风效果");
        }
        
        if (player.hasStatusEffect(ModStatusEffects.EXHAUSTION)) {
            diseases.put(ModStatusEffects.EXHAUSTION, "过度劳累");
            Bioresistance.LOGGER.info("发现过度劳累效果");
        }
        
        Bioresistance.LOGGER.info("玩家 {} 当前发现 {} 种疾病", player.getName().getString(), diseases.size());
        
        return diseases;
    }
    
    /**
     * 检查玩家当前的疾病状态并显示
     * @param source 命令源
     * @param player 玩家实体
     * @return 命令执行结果
     */
    private static int checkPlayerDiseases(ServerCommandSource source, PlayerEntity player) {
        Map<StatusEffect, String> diseases = getPlayerDiseases(player);
        
        if (diseases.isEmpty()) {
            source.sendFeedback(() -> Text.literal("玩家 " + player.getName().getString() + " 当前没有任何疾病效果"), false);
            return 1;
        }
        
        source.sendFeedback(() -> Text.literal("玩家 " + player.getName().getString() + " 当前患有以下疾病："), false);
        
        // 显示每种疾病及其剩余时间
        for (Map.Entry<StatusEffect, String> entry : diseases.entrySet()) {
            StatusEffectInstance effect = player.getStatusEffect(entry.getKey());
            if (effect != null) {
                int seconds = effect.getDuration() / 20; // 游戏刻转换为秒
                source.sendFeedback(() -> Text.literal("- " + entry.getValue() + "：剩余 " + 
                    (seconds >= 60 ? (seconds / 60) + "分" + (seconds % 60) + "秒" : seconds + "秒")), false);
            }
        }
        
        return 1;
    }

    private static int applyEffectCommand(CommandContext<ServerCommandSource> context, PlayerEntity target, String effectNameParam) {
        ServerCommandSource source = context.getSource();
        StatusEffect effectToApply = null;
        String effectDisplayName;
        
        // 根据效果名称选择效果
        switch (effectNameParam.toLowerCase()) {
            case "tetanus":
                effectToApply = ModStatusEffects.TETANUS;
                effectDisplayName = "Tetanus";
                break;
            case "plague":
                effectToApply = ModStatusEffects.PLAGUE;
                effectDisplayName = "Plague";
                break;
            case "exhaustion":
                effectToApply = ModStatusEffects.EXHAUSTION;
                effectDisplayName = "Exhaustion";
                break;
            case "candidiasis":
                // 使用CandidiasisEventHandler.applyCandidiasisEffect替代直接应用效果
                CandidiasisEventHandler.applyCandidiasisEffect(target);
                effectDisplayName = "Candidiasis";
                return 1; // 直接返回成功，因为已经在applyCandidiasisEffect中应用了效果
            default:
                source.sendError(Text.literal("未知效果: " + effectNameParam));
                return 0;
        }

        // 应用效果
        if (effectToApply != null) {
            final String finalEffectName = effectDisplayName; // 创建final变量用于lambda表达式
            target.addStatusEffect(new StatusEffectInstance(
                effectToApply, 
                6000,  // 5分钟
                0, 
                false, 
                true, 
                false
            ));
            source.sendFeedback(() -> Text.literal("已对玩家 " + target.getName().getString() + " 应用 " + finalEffectName + " 效果"), true);
            return 1;
        } else {
            source.sendError(Text.literal("无法应用效果: " + effectNameParam));
            return 0;
        }
    }
} 