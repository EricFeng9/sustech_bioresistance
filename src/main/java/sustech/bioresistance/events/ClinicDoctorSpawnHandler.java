package sustech.bioresistance.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.ModWorldGen;
import sustech.bioresistance.entities.DoctorEntity;

import java.util.HashMap;
import java.util.Map;

public class ClinicDoctorSpawnHandler {
    // 记录每个诊所位置和其中的医生是否已经生成
    private static final Map<String, Boolean> clinicDoctorSpawned = new HashMap<>();
    
    // 创建诊所结构标签
    public static final TagKey<Structure> CLINIC = TagKey.of(RegistryKeys.STRUCTURE, new Identifier("bio-resistance", "clinic"));
    
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 对每个世界分别处理
            for (ServerWorld world : server.getWorlds()) {
                // 只在主世界处理
                if (world.getRegistryKey() == ServerWorld.OVERWORLD) {
                    checkAndSpawnDoctors(world);
                }
            }
        });
        
        Bioresistance.LOGGER.info("诊所医生生成处理器已注册");
    }
    
    private static void checkAndSpawnDoctors(ServerWorld world) {
        // 获取当前游戏时间
        long currentTime = world.getTime();
        
        // 每5秒执行一次检查 (100游戏刻)
        if (currentTime % 100 != 0) {
            return;
        }
        
        // 获取所有在线玩家
        world.getPlayers().forEach(player -> {
            // 检查玩家附近是否有诊所结构
            BlockPos playerPos = player.getBlockPos();
            
            // 检查离玩家最近的诊所
            BlockPos clinicPos = world.locateStructure(
                    CLINIC,
                    playerPos, 
                    5, // 搜索半径（区块）
                    false // 如果找不到也不要生成新的
            );
            
            if (clinicPos != null) {
                // 计算诊所中心与玩家的距离
                double distance = Math.sqrt(playerPos.getSquaredDistance(clinicPos));
                
                // 只有当玩家在诊所附近时才检查（64格内）
                if (distance <= 64) {
                    // 生成诊所的唯一标识符 (维度ID + 坐标)
                    String clinicId = world.getRegistryKey().getValue() + "@" + 
                                     clinicPos.getX() + "," + clinicPos.getY() + "," + clinicPos.getZ();
                    
                    // 检查这个诊所是否已经生成过医生
                    Boolean hasDoctor = clinicDoctorSpawned.get(clinicId);
                    
                    // 如果没有生成过医生，或者没有记录，则尝试生成
                    if (hasDoctor == null || !hasDoctor) {
                        // 在诊所中心生成医生
                        DoctorEntity.spawnDoctorInClinic(world, clinicPos);
                        
                        // 标记这个诊所已经生成了医生
                        clinicDoctorSpawned.put(clinicId, true);
                        
                        Bioresistance.LOGGER.info("在诊所 [" + clinicPos.getX() + ", " + 
                                clinicPos.getY() + ", " + clinicPos.getZ() + "] 生成了医生");
                    }
                }
            }
        });
    }
} 