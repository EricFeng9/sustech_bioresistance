package sustech.bioresistance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
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



public class Bioresistance implements ModInitializer {
	public static final String MOD_ID = "bio-resistance";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            LOGGER.info("UseBlockCallback triggered");
            ItemStack heldItem = player.getStackInHand(hand);
            if (heldItem.getItem() == ModItems.EXPLANATORY_LIQUID_BP || heldItem.getItem() == ModItems.EXPLANATORY_LIQUID_B) {
                LOGGER.info("Player is holding EXPLANATORY_LIQUID_BP");
                BlockPos blockPos = hitResult.getBlockPos();
                BlockState blockState = world.getBlockState(blockPos);

                if (blockState.getBlock() == ModBlocks.POLLUTION_BLOCK) {
                    LOGGER.info("Pollution block detected, replacing with grass block");
                    world.setBlockState(blockPos, Blocks.GRASS_BLOCK.getDefaultState());
                    LOGGER.info("Block replaced at position: " + blockPos);

                    if (!player.isCreative()) {
                        heldItem.decrement(1);
                    }

                    return ActionResult.SUCCESS;
                }
            }
            if (heldItem.getItem() == ModItems.EXPLANATORY_LIQUID_P) {
                LOGGER.info("Player is holding EXPLANATORY_LIQUID_P");
                BlockPos centerPos = hitResult.getBlockPos();

                // 遍历 3×3×3 区域
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos targetPos = centerPos.add(x, y, z);
                            BlockState blockState = world.getBlockState(targetPos);

                            // 检查是否是污染方块
                            if (blockState.getBlock() == ModBlocks.POLLUTION_BLOCK) {
                                LOGGER.info("Pollution block detected at position: " + targetPos);
                                // 将污染方块替换为草地方块
                                world.setBlockState(targetPos, Blocks.GRASS_BLOCK.getDefaultState());
                                LOGGER.info("Block replaced at position: " + targetPos);

                                // 添加粒子效果
                                world.addParticle(ParticleTypes.HAPPY_VILLAGER, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 1, 1, 1);

                                // 播放音效
                                world.playSound(player, targetPos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
                            }
                        }
                    }
                }

                // 消耗降解液BP（可选）
                if (!player.isCreative()) {
                    heldItem.decrement(1);
                }

                // 返回成功，阻止默认行为
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        Bio_Fridge.initialize();
        ModEntityTypes.initialize();
        ModFluids.initialize();
        ModScreenHandlers.registerAll();
        ModStatusEffects.initialize();

        LOGGER.info("Hello Fabric world!");
    }
}