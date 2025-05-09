package sustech.bioresistance.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;

/**
 * 耐药性数据网络同步类
 * 负责将服务端的耐药性数据同步到客户端
 */
public class ResistanceSync {
    
    // 网络通道标识符
    public static final Identifier TETANUS_RESISTANCE_SYNC = new Identifier("bio-resistance", "tetanus_resistance_sync");
    public static final Identifier PLAGUE_RESISTANCE_SYNC = new Identifier("bio-resistance", "plague_resistance_sync");
    public static final Identifier CANDIDA_RESISTANCE_SYNC = new Identifier("bio-resistance", "candida_resistance_sync");
    
    /**
     * 向所有在线玩家发送破伤风杆菌耐药性数据
     * @param server Minecraft服务器实例
     * @param resistance 当前耐药性值
     */
    public static void syncTetanusResistanceToAll(MinecraftServer server, float resistance) {
        if (server == null) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(resistance);
        
        // 向所有在线玩家发送数据包
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, TETANUS_RESISTANCE_SYNC, buf);
        }
        
        Bioresistance.LOGGER.debug("已向所有玩家同步破伤风杆菌耐药性: {}", String.format("%.1f%%", resistance * 100));
    }
    
    /**
     * 向所有在线玩家发送鼠疫耶尔森菌耐药性数据
     * @param server Minecraft服务器实例
     * @param resistance 当前耐药性值
     */
    public static void syncPlagueResistanceToAll(MinecraftServer server, float resistance) {
        if (server == null) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(resistance);
        
        // 向所有在线玩家发送数据包
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, PLAGUE_RESISTANCE_SYNC, buf);
        }
        
        Bioresistance.LOGGER.debug("已向所有玩家同步鼠疫耶尔森菌耐药性: {}", String.format("%.1f%%", resistance * 100));
    }
    
    /**
     * 向所有在线玩家发送耳念珠菌耐药性数据
     * @param server Minecraft服务器实例
     * @param resistance 当前耐药性值
     */
    public static void syncCandidaResistanceToAll(MinecraftServer server, float resistance) {
        if (server == null) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(resistance);
        
        // 向所有在线玩家发送数据包
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, CANDIDA_RESISTANCE_SYNC, buf);
        }
        
        Bioresistance.LOGGER.debug("已向所有玩家同步耳念珠菌耐药性: {}", String.format("%.1f%%", resistance * 100));
    }
    
    /**
     * 向单个玩家发送所有耐药性数据
     * 用于玩家加入服务器时同步数据
     * @param player 目标玩家
     * @param tetanusResistance 破伤风杆菌耐药性
     * @param plagueResistance 鼠疫耶尔森菌耐药性
     * @param candidaResistance 耳念珠菌耐药性
     */
    public static void syncAllResistancesToPlayer(ServerPlayerEntity player, 
                                              float tetanusResistance, 
                                              float plagueResistance, 
                                              float candidaResistance) {
        if (player == null) return;
        
        // 发送破伤风杆菌耐药性
        PacketByteBuf tetanusBuf = PacketByteBufs.create();
        tetanusBuf.writeFloat(tetanusResistance);
        ServerPlayNetworking.send(player, TETANUS_RESISTANCE_SYNC, tetanusBuf);
        
        // 发送鼠疫耶尔森菌耐药性
        PacketByteBuf plagueBuf = PacketByteBufs.create();
        plagueBuf.writeFloat(plagueResistance);
        ServerPlayNetworking.send(player, PLAGUE_RESISTANCE_SYNC, plagueBuf);
        
        // 发送耳念珠菌耐药性
        PacketByteBuf candidaBuf = PacketByteBufs.create();
        candidaBuf.writeFloat(candidaResistance);
        ServerPlayNetworking.send(player, CANDIDA_RESISTANCE_SYNC, candidaBuf);
        
        Bioresistance.LOGGER.debug("已向玩家 {} 同步所有耐药性数据", player.getName().getString());
    }
} 