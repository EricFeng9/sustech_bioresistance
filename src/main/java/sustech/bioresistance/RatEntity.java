package sustech.bioresistance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;

public class RatEntity extends HostileEntity {
    private static final TrackedData<Boolean> HAS_PLAGUE = DataTracker.registerData(RatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public RatEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createRatAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HAS_PLAGUE, this.random.nextFloat() < 0.3f); // 30%几率携带鼠疫
    }

    public boolean hasPlague() {
        return this.dataTracker.get(HAS_PLAGUE);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean attacked = super.tryAttack(target);
        if (attacked && target instanceof LivingEntity && this.hasPlague()) {
            // 如果攻击成功且老鼠携带鼠疫，有几率感染目标
            if (this.random.nextFloat() < 0.4f) { // 40%感染几率
                ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(
                        StatusEffectsRegistry.PLAGUE, // 需要注册自定义状态效果
                        600, // 30秒
                        0
                ));
            }
        }
        return attacked;
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        if (causedByPlayer) {
            // 掉落生老鼠肉
            this.dropItem(ModItems.RAW_RAT_MEAT, this.random.nextInt(2) + 1); // 掉落1-2个生老鼠肉
        }
    }
}