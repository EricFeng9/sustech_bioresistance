package sustech.bioresistance;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
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
    
    public static final EntityType<RatEntity> RAT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("examplemod", "rat"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, RatEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build()
    );


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
        FabricDefaultAttributeRegistry.register(RAT, RatEntity.createRatAttributes());
        // 注册破伤风事件处理器
        TetanusEventHandler.register();
        
        // 注册模组命令
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> 
                sustech.bioresistance.commands.BioresistanceCommands.register(dispatcher, registryAccess, environment)
        );
        LOGGER.info("你好")；
        LOGGER.info("Hello Fabric world!");
    }
}