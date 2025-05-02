package sustech.bioresistance.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import sustech.bioresistance.Bioresistance;

/**
 * 鼠疫耶尔森菌耐药性数据管理器
 * 负责存储和管理全局的鼠疫耶尔森菌对链霉素的耐药性数据
 */
public class PlagueResistanceManager extends PersistentState {
    
    // 耐药性数据标识
    private static final String RESISTANCE_KEY = "plague_resistance";
    // 每次使用链霉素增加的耐药性百分比（0.1% - 0.001f）
    private static final float RESISTANCE_INCREMENT = 0.001f;
    // 当前鼠疫耶尔森菌对链霉素的耐药性（0.0-1.0）
    private float resistance = 0.0f;
    
    // 用于客户端显示的静态缓存
    private static float cachedResistance = 0.0f;
    
    /**
     * 创建一个新的耐药性管理器实例
     */
    public PlagueResistanceManager() {
        super();
    }
    
    /**
     * 从NBT数据中加载耐药性数据
     * @param nbt NBT数据
     * @return 加载的管理器实例
     */
    public static PlagueResistanceManager createFromNbt(NbtCompound nbt) {
        PlagueResistanceManager manager = new PlagueResistanceManager();
        manager.resistance = nbt.getFloat(RESISTANCE_KEY);
        return manager;
    }
    
    /**
     * 获取当前鼠疫耶尔森菌的耐药性（0.0-1.0）
     * @return 当前耐药性值
     */
    public float getResistance() {
        return resistance;
    }
    
    /**
     * 设置鼠疫耶尔森菌的耐药性值
     * @param value 新的耐药性值（0.0-1.0）
     * @return 是否设置成功
     */
    public boolean setResistance(float value) {
        // 确保值在有效范围内
        value = Math.max(0.0f, Math.min(1.0f, value));
        
        try {
            // 设置新值
            this.resistance = value;
            // 更新缓存
            updateCache();
            // 标记数据已修改
            this.markDirty();
            
            Bioresistance.LOGGER.info("鼠疫耶尔森菌耐药性已通过命令设置为 {}", getResistancePercentage());
            return true;
        } catch (Exception e) {
            Bioresistance.LOGGER.error("设置鼠疫耶尔森菌耐药性失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取当前鼠疫耶尔森菌的耐药性（百分比格式，0-100%）
     * @return 耐药性百分比字符串
     */
    public String getResistancePercentage() {
        // 将小数转换为百分比并保留1位小数
        return String.format("%.1f%%", resistance * 100);
    }
    
    /**
     * 增加鼠疫耶尔森菌的耐药性
     * 每次使用链霉素治疗时调用
     */
    public void increaseResistance() {
        resistance += RESISTANCE_INCREMENT;
        if (resistance > 1.0f) {
            resistance = 1.0f; // 最大不超过100%
        }
        // 更新缓存值
        updateCache();
        
        this.markDirty(); // 标记数据已修改，需要保存
        Bioresistance.LOGGER.info("鼠疫耶尔森菌耐药性增加到 {}", getResistancePercentage());
    }
    
    /**
     * 获取缓存的耐药性值，用于客户端显示
     * @return 缓存的耐药性百分比文本
     */
    public static String getCachedResistancePercentage() {
        Bioresistance.LOGGER.debug("获取缓存的耐药性: {}", String.format("%.1f%%", cachedResistance * 100));
        return String.format("%.1f%%", cachedResistance * 100);
    }
    
    /**
     * 更新缓存值
     */
    private void updateCache() {
        cachedResistance = resistance;
        Bioresistance.LOGGER.debug("已更新耐药性缓存到 {}", String.format("%.1f%%", cachedResistance * 100));
    }
    
    /**
     * 判断基于当前耐药性的治疗是否会失败
     * @return 如果治疗失败返回true，成功返回false
     */
    public boolean willTreatmentFail() {
        // 随机数小于耐药性则治疗失败
        return Math.random() < resistance;
    }
    
    /**
     * 将数据序列化为NBT
     * @param nbt NBT数据对象
     * @return 包含数据的NBT对象
     */
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putFloat(RESISTANCE_KEY, resistance);
        return nbt;
    }
    
    /**
     * 定义如何从NBT中读取和创建状态对象的Type
     */
    public static final PersistentState.Type<PlagueResistanceManager> TYPE = 
        new PersistentState.Type<>(
            () -> new PlagueResistanceManager(),
            PlagueResistanceManager::createFromNbt,
            null
        );
    
    /**
     * 从服务器获取耐药性管理器实例
     * @param server MinecraftServer实例
     * @return 耐药性管理器实例
     */
    public static PlagueResistanceManager getManager(MinecraftServer server) {
        // 从服务器的主世界持久状态管理器获取数据
        PlagueResistanceManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager()
            .getOrCreate(TYPE, "plague_resistance");
        // 更新缓存值
        cachedResistance = manager.resistance;
        Bioresistance.LOGGER.debug("从服务器加载耐药性并更新缓存: {}", String.format("%.1f%%", cachedResistance * 100));
        return manager;
    }
} 