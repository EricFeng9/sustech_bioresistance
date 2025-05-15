package sustech.bioresistance.entities;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.Heightmap;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import sustech.bioresistance.ModItems;
import sustech.bioresistance.events.PlagueEventHandler;
import sustech.bioresistance.ModEntities;

public class RatEntity extends AnimalEntity implements GeoEntity {
    private static final TrackedData<Boolean> ANGRY = DataTracker.registerData(RatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int angerTime;
    private UUID angryAt;
    
    // GeckoLib动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // 预定义动画
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    public RatEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    // GeckoLib动画注册
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }

    // 动画状态机控制器
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> state) {
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }
    
    // 攻击动画控制器
    private <T extends GeoAnimatable> PlayState attackPredicate(AnimationState<T> state) {
        if (this.isAttacking()) {
            return state.setAndContinue(ATTACK_ANIM);
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 用于触发攻击动画 - 更改为public以解决编译错误
    @Override
    public boolean isAttacking() {
        return this.handSwinging;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
        // 添加躲避猫和豹猫的行为
        this.goalSelector.add(2, new FleeEntityGoal<>(this, CatEntity.class, 10.0F, 1.2, 1.5));
        this.goalSelector.add(2, new FleeEntityGoal<>(this, OcelotEntity.class, 10.0F, 1.2, 1.5));
        // 更趋向于黑暗区域
        this.goalSelector.add(3, new FleeEntityGoal<>(this, PlayerEntity.class, 8.0F, 1.0, 1.2));
        this.goalSelector.add(3, new AvoidSunlightGoal(this));
        this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(6, new LookAroundGoal(this));
        
        // 当愤怒时才会攻击玩家
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true, entity -> 
            this.isAngry() && (this.getAngryAt() == null || this.getAngryAt().equals(entity.getUuid()))
        ));
        
        // 主动攻击僵尸类敌人
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, ZombieEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, DrownedEntity.class, true));
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, ZombifiedPiglinEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createRatAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    public static boolean canSpawnInDark(EntityType<RatEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).allowsSpawning(world, pos.down(), type) && 
               world.getLightLevel(pos) <= 7;
    }

    // 村庄专用生成检查方法，不受光照限制
    public static boolean canSpawnInVillage(EntityType<RatEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).allowsSpawning(world, pos.down(), type);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(ANGRY, false);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean attacked = super.tryAttack(target);
        if (attacked && target instanceof LivingEntity) {
            // 老鼠攻击目标时，有1%概率感染鼠疫
            if (this.getRandom().nextFloat() < 0.01f) { // 1% -0.01f感染几率
                // 使用PlagueEventHandler来应用鼠疫效果
                PlagueEventHandler.applyPlagueEffect((LivingEntity)target);
            }
        }
        return attacked;
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        if (causedByPlayer) {
            // 掉落生老鼠肉
            this.dropItem(ModItems.RAW_RAT_MEAT);
        }
    }

    public boolean isAngry() {
        return this.dataTracker.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        this.dataTracker.set(ANGRY, angry);
    }

    public void setAngerTime(int ticks) {
        this.angerTime = ticks;
    }

    public int getAngerTime() {
        return this.angerTime;
    }

    public void setAngryAt(UUID uuid) {
        this.angryAt = uuid;
    }

    @Nullable
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            if (this.isAngry()) {
                if (this.getAngerTime() <= 0) {
                    this.setAngry(false);
                    this.setAngryAt(null);
                } else {
                    this.setAngerTime(this.getAngerTime() - 1);
                }
            }
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity && !this.isAngry()) {
            this.setAngry(true);
            this.setAngerTime(200 + this.getRandom().nextInt(200)); // 10-20秒的愤怒时间
            this.setAngryAt(((PlayerEntity) source.getAttacker()).getUuid());
        }
        return super.damage(source, amount);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_DEATH;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false; // 老鼠不能繁殖
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null; // 老鼠不能繁殖
    }

    // 用于强制生成更多老鼠的静态方法
    public static void forceSpawnRatsInVillage(ServerWorld world, BlockPos villageCenter, int radius) {
        // 在村庄中心周围随机生成3-5只老鼠
        int ratsToSpawn = 3 + world.random.nextInt(2); // 3到5只
        
        for (int i = 0; i < ratsToSpawn; i++) {
            // 在村庄中心的半径范围内随机选择一个位置
            int x = villageCenter.getX() + world.random.nextInt(radius * 2) - radius;
            int z = villageCenter.getZ() + world.random.nextInt(radius * 2) - radius;
            
            // 寻找合适的Y坐标（地面）
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos spawnPos = new BlockPos(x, y, z);
            
            // 检查生成位置是否适合
            if (canSpawnInVillage(ModEntities.RAT, world, SpawnReason.NATURAL, spawnPos, world.random)) {
                // 生成老鼠实体
                RatEntity rat = ModEntities.RAT.create(world);
                if (rat != null) {
                    rat.refreshPositionAndAngles(spawnPos, 0.0F, 0.0F);
                    world.spawnEntityAndPassengers(rat);
                }
            }
        }
    }
}