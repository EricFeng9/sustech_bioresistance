package sustech.bioresistance.complexBlocks;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sustech.bioresistance.ImplementedInventory;
import sustech.bioresistance.ModEntityTypes;
import sustech.bioresistance.ModItems;

import java.util.Random;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class CleanTable_Entity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private int cookTime; // 当前处理进度
    private final int cookTimeTotal = 100; // 总处理时间，100游戏刻，10s
    private static final Random RANDOM = new Random();
    
    // 跟踪正在交互的玩家
    private final List<UUID> interactingPlayers = new ArrayList<>();

    // 属性委托同步数据到客户端
    public final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(2) {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) cookTime = value;
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public CleanTable_Entity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.CleanTable, pos, state);
    }

    /**
     * 添加正在交互的玩家
     * @param playerUUID 玩家UUID
     */
    public void addInteractingPlayer(UUID playerUUID) {
        if (!interactingPlayers.contains(playerUUID)) {
            interactingPlayers.add(playerUUID);
        }
    }

    /**
     * 移除不再交互的玩家
     * @param playerUUID 玩家UUID
     */
    public void removeInteractingPlayer(UUID playerUUID) {
        interactingPlayers.remove(playerUUID);
    }

    /**
     * 检查是否有玩家正在与工作台交互
     * @return 如果有玩家正在交互则返回true
     */
    public boolean hasInteractingPlayers() {
        return !interactingPlayers.isEmpty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, CleanTable_Entity entity) {
        if (world.isClient) return;

        // 只有当有玩家正在交互时才进行制作
        if (entity.hasInteractingPlayers()) {
            boolean canCraft = canCraft(entity);

            if (canCraft) {
                entity.cookTime++;
                if (entity.cookTime >= entity.cookTimeTotal) {
                    craftItem(entity);
                    entity.cookTime = 0;
                }
                markDirty(world, pos, state);
            } else {
                entity.cookTime = 0;
                markDirty(world, pos, state);
            }
        } else if (entity.cookTime > 0) {
            // 如果没有玩家交互但进度条不为0，则重置进度
            entity.cookTime = 0;
            markDirty(world, pos, state);
        }
    }

    private static boolean canCraft(CleanTable_Entity entity) {
        ItemStack slot1 = entity.getStack(SLOT_1);
        ItemStack slot2 = entity.getStack(SLOT_2);
        ItemStack outputSlot = entity.getStack(OUTPUT_SLOT);
        
        // 检查输出槽是否为空，或者输出槽物品是否可以继续堆叠
        if (outputSlot.isEmpty() || outputSlot.getCount() < outputSlot.getMaxCount()) {
            // 预检查：识别当前合成的结果类型
            
            // 1. 抗生素分泌菌提取液 + 注射器 = 随机产出药物
            // 注意：由于这个是随机产出，不适合检查输出槽是否匹配，我们只有在输出槽为空时才允许此合成
            if (slot1.isOf(ModItems.SYRINGE) && slot2.isOf(ModItems.ANTIBIOTIC_BACTERIA_EXTRACT)) {
                return outputSlot.isEmpty(); // 只有输出槽为空时才能合成随机药物
            }
            
            // 2. 水凝胶 + 装配T6SS的大肠杆菌培养基 = 抗耐药性微生物软膏
            if (slot1.isOf(ModItems.HYDROGEL) && slot2.isOf(ModItems.E_COLI_T6SS_MEDIUM)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
            
            // 3. 胶囊 + 装配T6SS的大肠杆菌培养基 = 抗耐药性微生物胶囊
            if (slot1.isOf(ModItems.EMPTY_CAPSULE) && slot2.isOf(ModItems.E_COLI_T6SS_MEDIUM)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
            
            // 4a. 空培养基 + 西瓜 = AC菌培养基
            if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(Items.MELON_SLICE)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
            
            // 4b. 空培养基 + AC菌培养基 = AC菌培养基
            if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
            
            // 5a. 空培养基 + 大肠杆菌提取液 = 大肠杆菌培养基
            if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.E_COLI_EXTRACT)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.E_COLI_MEDIUM) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
            
            // 5b. 空培养基 + 大肠杆菌培养基 = 大肠杆菌培养基
            if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.E_COLI_MEDIUM)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.E_COLI_MEDIUM) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
            
            // 6. 空培养基 + 装配T6SS的大肠杆菌培养基 = 装配T6SS的大肠杆菌培养基
            if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.E_COLI_T6SS_MEDIUM)) {
                return outputSlot.isEmpty() || 
                       (outputSlot.isOf(ModItems.E_COLI_T6SS_MEDIUM) && 
                        outputSlot.getCount() < outputSlot.getMaxCount());
            }
        }
        
        return false;
    }

    private static void craftItem(CleanTable_Entity entity) {
        ItemStack slot1 = entity.getStack(SLOT_1);
        ItemStack slot2 = entity.getStack(SLOT_2);
        ItemStack outputSlot = entity.getStack(OUTPUT_SLOT);
        
        // 1. 抗生素分泌菌提取液 + 注射器 = 随机产出药物
        if (slot1.isOf(ModItems.SYRINGE) && slot2.isOf(ModItems.ANTIBIOTIC_BACTERIA_EXTRACT)) {
            slot1.decrement(1);
            slot2.decrement(1);
            
            // 随机决定产出哪种药物
            int randomNum = RANDOM.nextInt(3);
            ItemStack result = switch(randomNum) {
                case 0 -> new ItemStack(ModItems.METRONIDAZOLE);
                case 1 -> new ItemStack(ModItems.STREPTOMYCIN);
                case 2 -> new ItemStack(ModItems.ANTIFUNGAL_DRUG);
                default -> new ItemStack(ModItems.METRONIDAZOLE);
            };
            
            entity.setStack(OUTPUT_SLOT, result);
            return;
        }
        
        // 对于下面的配方，支持输出堆叠
        // 2. 水凝胶 + 装配T6SS的大肠杆菌培养基 = 抗耐药性微生物软膏
        if (slot1.isOf(ModItems.HYDROGEL) && slot2.isOf(ModItems.E_COLI_T6SS_MEDIUM)) {
            slot1.decrement(1);
            slot2.decrement(1);
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_OINTMENT));
            } else {
                outputSlot.increment(1); // 增加一个到现有堆
            }
            return;
        }
        
        // 3. 胶囊 + 装配T6SS的大肠杆菌培养基 = 抗耐药性微生物胶囊
        if (slot1.isOf(ModItems.EMPTY_CAPSULE) && slot2.isOf(ModItems.E_COLI_T6SS_MEDIUM)) {
            slot1.decrement(1);
            slot2.decrement(1);
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.ANTI_DRUG_RESISTANT_MICROBIAL_CAPSULES));
            } else {
                outputSlot.increment(1);
            }
            return;
        }
        
        // 4a. 空培养基 + 西瓜 = AC菌培养基
        if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(Items.MELON_SLICE)) {
            slot1.decrement(1);
            slot2.decrement(1);
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.ACIDOVORAX_CITRULLI_MEDIUM));
            } else {
                outputSlot.increment(1);
            }
            return;
        }
        
        // 4b. 空培养基 + AC菌培养基 = AC菌培养基
        if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM)) {
            slot1.decrement(1);
            // 不消耗菌种来源，保留AC菌培养基
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.ACIDOVORAX_CITRULLI_MEDIUM));
            } else {
                outputSlot.increment(1);
            }
            return;
        }
        
        // 5a. 空培养基 + 大肠杆菌提取液 = 大肠杆菌培养基
        if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.E_COLI_EXTRACT)) {
            slot1.decrement(1);
            slot2.decrement(1);
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.E_COLI_MEDIUM));
            } else {
                outputSlot.increment(1);
            }
            return;
        }
        
        // 5b. 空培养基 + 大肠杆菌培养基 = 大肠杆菌培养基
        if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.E_COLI_MEDIUM)) {
            slot1.decrement(1);
            // 不消耗菌种来源，保留大肠杆菌培养基
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.E_COLI_MEDIUM));
            } else {
                outputSlot.increment(1);
            }
            return;
        }
        
        // 6. 空培养基 + 装配T6SS的大肠杆菌培养基 = 装配T6SS的大肠杆菌培养基
        if (slot1.isOf(ModItems.MEDIUM_STERILIZED) && slot2.isOf(ModItems.E_COLI_T6SS_MEDIUM)) {
            slot1.decrement(1);
            // 不消耗菌种来源，保留装配T6SS的大肠杆菌培养基
            
            if (outputSlot.isEmpty()) {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.E_COLI_T6SS_MEDIUM));
            } else {
                outputSlot.increment(1);
            }
            return;
        }
    }

    // 实现 Inventory 接口方法
    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);
        nbt.putInt("CookTime", cookTime);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
        cookTime = nbt.getInt("CookTime");
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.clean_table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // 玩家打开界面时添加到交互列表
        addInteractingPlayer(player.getUuid());
        return new CleanTable_ScreenHandler(syncId, playerInventory, this, player);
    }

    @Override
    public void markRemoved() {
        // 方块实体被移除时清空交互玩家列表
        interactingPlayers.clear();
        super.markRemoved();
    }

    public static final int SLOT_1 = 0;  // 输入槽位1 (注射器/水凝胶/胶囊/空培养基)
    public static final int SLOT_2 = 1;  // 输入槽位2 (各种液体/培养基)
    public static final int OUTPUT_SLOT = 2;  // 输出槽位

    // Inventory 方法继承自 ImplementedInventory，无需重复实现
}