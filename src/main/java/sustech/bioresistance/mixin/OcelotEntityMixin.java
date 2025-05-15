package sustech.bioresistance.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sustech.bioresistance.entities.RatEntity;

@Mixin(OcelotEntity.class)
public abstract class OcelotEntityMixin extends AnimalEntity {

    // 构造函数，继承自AnimalEntity
    protected OcelotEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    // 注入到initGoals方法，添加攻击老鼠的AI目标
    @Inject(method = "initGoals", at = @At("TAIL"))
    private void addRatAttackGoal(CallbackInfo ci) {
        // 添加针对老鼠的近战攻击行为
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.2D, true));
        
        // 添加将老鼠作为主动攻击目标的行为
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, RatEntity.class, true));
    }
} 