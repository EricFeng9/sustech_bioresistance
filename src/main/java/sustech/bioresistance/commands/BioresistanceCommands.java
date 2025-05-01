package sustech.bioresistance.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.data.TetanusResistanceManager;

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
        LiteralArgumentBuilder<ServerCommandSource> mainCommand = CommandManager.literal("bioresistance")
            .requires(source -> source.hasPermissionLevel(2)); // 需要管理员权限（等级2）

        // 添加help子命令
        mainCommand.then(CommandManager.literal("help")
            .executes(BioresistanceCommands::executeHelp));
        
        // 添加tetanus_resistance子命令
        mainCommand.then(CommandManager.literal("tetanus_resistance")
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                    .executes(BioresistanceCommands::executeSetTetanusResistance))
            )
        );
        
        // 注册到分发器
        dispatcher.register(mainCommand);
        
        Bioresistance.LOGGER.info("已注册生物抗性模组命令");
    }
    
    /**
     * 执行help命令
     */
    private static int executeHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 发送命令帮助信息
        source.sendFeedback(() -> Text.literal("=== Bio-Resistance Mod Commands ===")
            .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true)), false);
        
        source.sendFeedback(() -> Text.literal("/bioresistance help - Display all available commands")
            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), false);
        
        source.sendFeedback(() -> Text.literal("/bioresistance tetanus_resistance set <value> - Set tetanus bacteria resistance to metronidazole (value between 0-1)")
            .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)), false);
        
        return 1;
    }
    
    /**
     * 执行设置破伤风杆菌耐药性命令
     */
    private static int executeSetTetanusResistance(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // 获取值参数
        float value = FloatArgumentType.getFloat(context, "value");
        
        // 限制值在0-1范围内
        value = Math.max(0.0f, Math.min(1.0f, value));
        
        // 获取耐药性管理器并设置值
        TetanusResistanceManager manager = TetanusResistanceManager.getManager(source.getServer());
        boolean success = manager.setResistance(value);
        
        // 保存最终值用于lambda表达式
        final float finalValue = value;
        
        if (success) {
            // 发送成功反馈
            source.sendFeedback(() -> Text.literal("Tetanus bacteria resistance set to " + 
                String.format("%.1f%%", finalValue * 100))
                .setStyle(Style.EMPTY.withColor(Formatting.GREEN)), true);
        } else {
            // 发送失败反馈
            source.sendFeedback(() -> Text.literal("Failed to set tetanus bacteria resistance")
                .setStyle(Style.EMPTY.withColor(Formatting.RED)), true);
        }
        
        return success ? 1 : 0;
    }
} 