package sustech.bioresistance.entities;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
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
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.ModEntities;

public class DoctorEntity extends MerchantEntity implements GeoEntity {
    
    // GeckoLib动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    // 预定义动画
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation HEADUP_ANIM = RawAnimation.begin().thenPlay("headup");
    
    // 交易列表
    private TradeOfferList offers;
    
    // 添加动画状态追踪变量
    private boolean isInteracting = false;
    private int interactingTicks = 0;
    private static final int INTERACTION_DURATION = 40; // 互动动画持续2秒（40刻）

    public DoctorEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
        this.offers = new TradeOfferList();
    }

    // GeckoLib动画注册
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
        controllers.add(new AnimationController<>(this, "interactionController", 0, this::interactionPredicate));
    }

    // 基本动画状态机控制器（走路/站立）
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> state) {
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }
    
    // 交互动画控制器（头部动作）
    private <T extends GeoAnimatable> PlayState interactionPredicate(AnimationState<T> state) {
        if (isInteracting) {
            return state.setAndContinue(HEADUP_ANIM);
        }
        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(1, new LookAroundGoal(this));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.5));
        
        // 医生会被僵尸追击
        this.goalSelector.add(1, new FleeEntityGoal<>(this, ZombieEntity.class, 8.0F, 0.8, 0.8));
        
        // 僵尸会追击医生
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, DoctorEntity.class, true));
    }

    public static DefaultAttributeContainer.Builder createDoctorAttributes() {
        return MerchantEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }
    
    public static boolean canSpawnInClinic(EntityType<DoctorEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).allowsSpawning(world, pos.down(), type);
    }
    
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        // 如果玩家不是蹲着的，则打开交易界面
        if (!this.isAlive() || this.isRemoved() || this.isBaby() || player.isSneaking()) {
            return ActionResult.PASS;
        }
        
        // 设置正在互动状态，触发头部动画
        this.isInteracting = true;
        this.interactingTicks = INTERACTION_DURATION;
        
        if (!this.getWorld().isClient) {
            this.setCustomer(player);
            // 使用本地化的交易界面标题
            this.sendOffers(player, net.minecraft.text.Text.translatable("doctor.trade"), 1);
        }
        
        return ActionResult.success(this.getWorld().isClient);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 处理互动状态和计时
        if (this.isInteracting) {
            if (--this.interactingTicks <= 0) {
                this.isInteracting = false;
                this.interactingTicks = 0;
            }
        }
    }
    
    // 实现MerchantEntity的抽象方法
    @Override
    public void afterUsing(TradeOffer offer) {
        // 当交易完成后调用此方法
        offer.use(); // 增加交易次数
    }
    
    // 获取交易列表
    @Override
    public TradeOfferList getOffers() {
        if (this.offers.isEmpty()) {
            this.fillRecipes();
        }
        return this.offers;
    }
    
    // 设置交易列表（实现setOffers方法）
    public void setOffers(TradeOfferList offers) {
        this.offers = offers;
    }
    
    // 在实体创建时为医生填充交易列表
    protected void fillRecipes() {
        TradeOfferList tradeOffers = new TradeOfferList();
        
        // 链霉素交易 - 2绿宝石
        tradeOffers.add(new TradeOffer(
            new ItemStack(Items.EMERALD, 2),
            new ItemStack(ModItems.STREPTOMYCIN, 1),
            10, 2, 0.05f
        ));
        
        // 甲硝唑交易 - 2绿宝石
        tradeOffers.add(new TradeOffer(
            new ItemStack(Items.EMERALD, 2),
            new ItemStack(ModItems.METRONIDAZOLE, 1),
            10, 2, 0.05f
        ));
        
        // 抗真菌药交易 - 2绿宝石
        tradeOffers.add(new TradeOffer(
            new ItemStack(Items.EMERALD, 2),
            new ItemStack(ModItems.ANTIFUNGAL_DRUG, 1),
            10, 2, 0.05f
        ));
        
        // 注射器交易 - 1绿宝石
        tradeOffers.add(new TradeOffer(
            new ItemStack(Items.EMERALD, 1),
            new ItemStack(ModItems.SYRINGE, 1),
            16, 1, 0.05f
        ));
        
        // 空胶囊交易 - 1绿宝石
        tradeOffers.add(new TradeOffer(
            new ItemStack(Items.EMERALD, 1),
            new ItemStack(ModItems.EMPTY_CAPSULE, 1),
            16, 1, 0.05f
        ));
        
        this.setOffers(tradeOffers);
    }
    
    @Override
    public boolean isLeveledMerchant() {
        return false; // 医生不会升级
    }
    
    @Override
    public void onDeath(DamageSource source) {
        // 如果被僵尸杀死，有机会变成僵尸村民
        if (source.getAttacker() instanceof ZombieEntity && this.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)this.getWorld();
            VillagerEntity villager = EntityType.VILLAGER.create(serverWorld);
            if (villager != null) {
                villager.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
                villager.initialize(serverWorld, serverWorld.getLocalDifficulty(villager.getBlockPos()), SpawnReason.CONVERSION, null, null);
                
                // 转化为僵尸村民
                ZombieEntity zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, true);
                if (zombieVillager != null) {
                    Bioresistance.LOGGER.info("医生被僵尸杀死，变成了僵尸村民");
                }
                
                // 删除医生实体
                this.remove(RemovalReason.KILLED);
            }
        }
        super.onDeath(source);
    }
    
    // 在诊所中生成医生的静态方法
    public static void spawnDoctorInClinic(ServerWorld world, BlockPos clinicPos) {
        // 查找诊所内部的合适生成位置
        BlockPos spawnPos = findSuitableSpawnPos(world, clinicPos);
        if (spawnPos != null) {
            DoctorEntity doctor = ModEntities.DOCTOR.create(world);
            if (doctor != null) {
                doctor.refreshPositionAndAngles(spawnPos, 0, 0);
                doctor.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.STRUCTURE, null, null);
                world.spawnEntity(doctor);
                Bioresistance.LOGGER.info("在诊所 [" + clinicPos.getX() + ", " + 
                        clinicPos.getY() + ", " + clinicPos.getZ() + "] 生成了医生");
            }
        }
    }
    
    // 查找诊所内部的合适生成位置
    private static BlockPos findSuitableSpawnPos(ServerWorld world, BlockPos clinicCenter) {
        // 尝试在诊所中心点附近找一个合适的位置
        // 首先尝试中心点上方1格
        BlockPos centerUp = clinicCenter.up();
        if (isValidSpawnPos(world, centerUp)) {
            return centerUp;
        }
        
        // 然后在中心点周围3x3x3的范围内搜索
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y <= 2; y++) {
                    BlockPos pos = clinicCenter.add(x, y, z);
                    if (isValidSpawnPos(world, pos)) {
                        return pos;
                    }
                }
            }
        }
        
        // 如果没找到合适的位置，就用中心点（可能会导致医生卡在方块里）
        Bioresistance.LOGGER.warn("无法在诊所 [" + clinicCenter.getX() + ", " + 
                clinicCenter.getY() + ", " + clinicCenter.getZ() + "] 中找到适合医生生成的位置");
        return clinicCenter;
    }
    
    // 检查生成位置是否合法
    private static boolean isValidSpawnPos(ServerWorld world, BlockPos pos) {
        // 检查医生的生成位置（需要两格空间）
        return world.isAir(pos) && world.isAir(pos.up()) && 
               !world.isAir(pos.down()) && world.getBlockState(pos.down()).isSolid();
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null; // 医生不能繁殖
    }
} 