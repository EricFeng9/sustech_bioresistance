package sustech.bioresistance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import software.bernie.geckolib.GeckoLib;
import sustech.bioresistance.complexBlocks.Bio_Fridge;
import sustech.bioresistance.entities.RatEntity;
import sustech.bioresistance.events.CandidiasisEventHandler;
import sustech.bioresistance.events.PlagueEventHandler;
import sustech.bioresistance.events.TetanusEventHandler;

public class Bioresistance implements ModInitializer {
	public static final String MOD_ID = "bio-resistance";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        // 初始化GeckoLib
        GeckoLib.initialize();
        
        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        Bio_Fridge.initialize();
        ModEntityTypes.initialize();
        ModFluids.initialize();
        ModScreenHandlers.registerAll();
        ModStatusEffects.initialize();
        ModEntities.registerModEntities();
        FabricDefaultAttributeRegistry.register(ModEntities.RAT, RatEntity.createRatAttributes());
        // 初始化世界生成
        ModWorldGen.initialize();
        // 注册破伤风事件处理器
        TetanusEventHandler.register();
        // 注册鼠疫事件处理器
        PlagueEventHandler.register();
        // 注册耳念珠菌感染事件处理器
        CandidiasisEventHandler.register();
        // 注册村庄老鼠生成处理器
        sustech.bioresistance.events.VillageRatSpawnHandler.register();
        
        // 注册模组命令
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> 
                sustech.bioresistance.commands.BioresistanceCommands.register(dispatcher, registryAccess, environment)
        );
        LOGGER.info("Bio-resistance mod initialized!");
    }
}