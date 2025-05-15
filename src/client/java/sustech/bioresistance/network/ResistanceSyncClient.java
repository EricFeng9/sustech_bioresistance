package sustech.bioresistance.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.data.CandidaResistanceManager;
import sustech.bioresistance.data.PlagueResistanceManager;
import sustech.bioresistance.data.TetanusResistanceManager;

/**
 * 耐药性数据网络同步客户端类
 * 负责接收服务端发送的耐药性数据
 * 此类只在客户端环境加载
 */
@Environment(EnvType.CLIENT)
public class ResistanceSyncClient {
    
    /**
     * 注册客户端网络接收器
     * 在客户端初始化时调用
     */
    public static void registerClientReceivers() {
        // 接收破伤风杆菌耐药性数据
        ClientPlayNetworking.registerGlobalReceiver(
            ResistanceSync.TETANUS_RESISTANCE_SYNC, 
            (client, handler, buf, sender) -> {
                float resistance = buf.readFloat();
                // 在游戏线程中更新缓存
                client.execute(() -> {
                    TetanusResistanceManager.updateClientCache(resistance);
                    Bioresistance.LOGGER.debug("客户端已接收并更新破伤风杆菌耐药性: {}", String.format("%.1f%%", resistance * 100));
                });
            }
        );
        
        // 接收鼠疫耶尔森菌耐药性数据
        ClientPlayNetworking.registerGlobalReceiver(
            ResistanceSync.PLAGUE_RESISTANCE_SYNC, 
            (client, handler, buf, sender) -> {
                float resistance = buf.readFloat();
                // 在游戏线程中更新缓存
                client.execute(() -> {
                    PlagueResistanceManager.updateClientCache(resistance);
                    Bioresistance.LOGGER.debug("客户端已接收并更新鼠疫耶尔森菌耐药性: {}", String.format("%.1f%%", resistance * 100));
                });
            }
        );
        
        // 接收耳念珠菌耐药性数据
        ClientPlayNetworking.registerGlobalReceiver(
            ResistanceSync.CANDIDA_RESISTANCE_SYNC, 
            (client, handler, buf, sender) -> {
                float resistance = buf.readFloat();
                // 在游戏线程中更新缓存
                client.execute(() -> {
                    CandidaResistanceManager.updateClientCache(resistance);
                    Bioresistance.LOGGER.debug("客户端已接收并更新耳念珠菌耐药性: {}", String.format("%.1f%%", resistance * 100));
                });
            }
        );
        
        Bioresistance.LOGGER.info("客户端耐药性网络接收器注册成功");
    }
} 