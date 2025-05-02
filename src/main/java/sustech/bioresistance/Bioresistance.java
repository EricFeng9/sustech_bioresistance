package sustech.bioresistance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import sustech.bioresistance.complexBlocks.Bio_Fridge;
import sustech.bioresistance.events.TetanusEventHandler;



public class Bioresistance implements ModInitializer {
	public static final String MOD_ID = "bio-resistance";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        Bio_Fridge.initialize();
        ModEntityTypes.initialize();
        ModFluids.initialize();
        ModScreenHandlers.registerAll();
        ModStatusEffects.initialize();
        
        // 注册破伤风事件处理器
        TetanusEventHandler.register();
        
        // 注册模组命令
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> 
                sustech.bioresistance.commands.BioresistanceCommands.register(dispatcher, registryAccess, environment)
        );

        LOGGER.info("Hello Fabric world!");
    }
}