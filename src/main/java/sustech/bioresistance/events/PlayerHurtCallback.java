package sustech.bioresistance.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

/**
 * 玩家受伤回调接口
 * 当玩家受到伤害时触发
 * 返回值：
 * - SUCCESS 取消进一步处理并允许受伤
 * - PASS 继续处理
 * - FAIL 取消受伤
 */
public interface PlayerHurtCallback {
    Event<PlayerHurtCallback> EVENT = EventFactory.createArrayBacked(PlayerHurtCallback.class,
            (listeners) -> (entity, source, amount) -> {
                for (PlayerHurtCallback listener : listeners) {
                    ActionResult result = listener.onHurt(entity, source, amount);
                    
                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                
                return ActionResult.PASS;
            });

    /**
     * 当实体受伤时被调用
     * @param entity 受伤的实体
     * @param source 伤害源
     * @param amount 伤害数值
     * @return 处理结果
     */
    ActionResult onHurt(LivingEntity entity, DamageSource source, float amount);
} 