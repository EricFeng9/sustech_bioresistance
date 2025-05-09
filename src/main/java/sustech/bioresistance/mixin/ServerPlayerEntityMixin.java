package sustech.bioresistance.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.data.CandidaResistanceManager;
import sustech.bioresistance.data.PlagueResistanceManager;
import sustech.bioresistance.data.TetanusResistanceManager;
import sustech.bioresistance.network.ResistanceSync;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    
    // 在玩家首次加入游戏或重新连接时执行
    @Inject(method = "onSpawn", at = @At("TAIL"))
    private void onPlayerJoin(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // 确保更新耐药性缓存数据
        try {
            // 获取各种耐药性管理器
            TetanusResistanceManager tetanusManager = TetanusResistanceManager.getManager(player.getServer());
            PlagueResistanceManager plagueManager = PlagueResistanceManager.getManager(player.getServer());
            CandidaResistanceManager candidaManager = CandidaResistanceManager.getManager(player.getServer());
            
            // 向玩家同步所有耐药性数据
            ResistanceSync.syncAllResistancesToPlayer(
                player,
                tetanusManager.getResistance(),
                plagueManager.getResistance(),
                candidaManager.getResistance()
            );
            
            Bioresistance.LOGGER.info("玩家 {} 加入游戏，已同步所有耐药性数据", player.getName().getString());
        } catch (Exception e) {
            Bioresistance.LOGGER.error("玩家加入时同步耐药性数据失败：", e);
        }
    }
} 