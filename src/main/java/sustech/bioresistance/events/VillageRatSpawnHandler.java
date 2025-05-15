package sustech.bioresistance.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.entities.RatEntity;

import java.util.HashMap;
import java.util.Map;

public class VillageRatSpawnHandler {
    // 记录上次生成老鼠的游戏时间（每个维度分开记录）
    private static final Map<ServerWorld, Long> lastSpawnTime = new HashMap<>();
    // 村庄老鼠生成间隔（游戏刻）
    private static final long SPAWN_INTERVAL = 24000; // 一个游戏日（大约20分钟真实时间）
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 对每个世界分别处理
            for (ServerWorld world : server.getWorlds()) {
                // 只在主世界处理
                if (world.getRegistryKey() == ServerWorld.OVERWORLD) {
                    checkAndSpawnRats(world);
                }
            }
        });
        
        Bioresistance.LOGGER.info("村庄老鼠生成处理器已注册");
    }
    
    private static void checkAndSpawnRats(ServerWorld world) {
        // 获取当前游戏时间
        long currentTime = world.getTime();
        
        // 获取上次生成时间，如果没有则设为0
        long lastTime = lastSpawnTime.getOrDefault(world, 0L);
        
        // 如果已经过了生成间隔，则进行生成
        if (currentTime - lastTime >= SPAWN_INTERVAL) {
            // 更新上次生成时间
            lastSpawnTime.put(world, currentTime);
            
            // 获取所有在线玩家
            world.getPlayers().forEach(player -> {
                // 检查玩家附近是否有村庄
                BlockPos playerPos = player.getBlockPos();
                
                // 检查离玩家最近的村庄
                BlockPos villagePos = world.locateStructure(
                        StructureTags.VILLAGE, 
                        playerPos, 
                        5, // 搜索半径（区块）
                        false // 如果找不到也不要生成新的
                );
                
                if (villagePos != null) {
                    // 计算村庄中心与玩家的距离
                    double distance = Math.sqrt(playerPos.getSquaredDistance(villagePos));
                    
                    // 只有当玩家在村庄附近时才生成（256格内）
                    if (distance <= 256) {
                        // 在村庄中心生成老鼠
                        RatEntity.forceSpawnRatsInVillage(world, villagePos, 64);
                        
                        Bioresistance.LOGGER.info("在村庄 [" + villagePos.getX() + ", " + 
                                villagePos.getY() + ", " + villagePos.getZ() + "] 生成了老鼠群");
                    }
                }
            });
        }
    }
} 