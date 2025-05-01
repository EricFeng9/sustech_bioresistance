package sustech.bioresistance.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.data.TetanusResistanceManager;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    
    // 在玩家首次加入游戏或重新连接时执行
    @Inject(method = "onSpawn", at = @At("TAIL"))
    private void onPlayerJoin(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        
        // 确保更新耐药性缓存数据
        try {
            TetanusResistanceManager manager = TetanusResistanceManager.getManager(player.getServer());
            Bioresistance.LOGGER.info("玩家 {} 加入游戏，已更新耐药性数据缓存：{}", 
                                       player.getName().getString(), manager.getResistancePercentage());
        } catch (Exception e) {
            Bioresistance.LOGGER.error("玩家加入时更新耐药性缓存失败：", e);
        }
    }
} 