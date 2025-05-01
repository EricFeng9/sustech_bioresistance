package sustech.bioresistance.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.data.TetanusResistanceManager;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		// 这个方法在服务器启动并加载世界时被调用
		MinecraftServer server = (MinecraftServer)(Object)this;
		
		// 预加载耐药性数据，确保缓存正确初始化
		try {
			TetanusResistanceManager manager = TetanusResistanceManager.getManager(server);
			Bioresistance.LOGGER.info("已初始化破伤风耐药性数据：{}", manager.getResistancePercentage());
		} catch (Exception e) {
			Bioresistance.LOGGER.error("初始化破伤风耐药性数据时出错：", e);
		}
	}
}