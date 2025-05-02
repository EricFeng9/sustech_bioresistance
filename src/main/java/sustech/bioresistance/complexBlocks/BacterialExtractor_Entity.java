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
import sustech.bioresistance.ModFluids;
import sustech.bioresistance.ModItems;
import net.minecraft.util.ItemScatterer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BacterialExtractor_Entity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private int cookTime; // 当前处理进度
    private final int cookTimeTotal = 100; // 总处理时间，100游戏刻，10s
    private int selectedMode = 1; // 默认选择模式1
    
    // 跟踪正在交互的玩家
    private final List<UUID> interactingPlayers = new ArrayList<>();

    // 属性委托同步数据到客户端
    public final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(3) {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> cookTimeTotal;
                case 2 -> selectedMode;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookTime = value;
                case 2 -> selectedMode = value;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public BacterialExtractor_Entity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.BacterialExtractor, pos, state);
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

    /**
     * 切换选择模式
     * @param mode 要设置的模式 (1 或 2)
     */
    public void setSelectedMode(int mode) {
        if (mode == 1 || mode == 2) {
            this.selectedMode = mode;
            propertyDelegate.set(2, mode); // 确保同步到属性委托
            markDirty();
            
            // 如果在服务器端，通知客户端更新
            if (world != null && !world.isClient) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            }
        }
    }

    /**
     * 获取当前选择的模式
     * @return 当前模式 (1 或 2)
     */
    public int getSelectedMode() {
        return this.selectedMode;
    }

    public static void tick(World world, BlockPos pos, BlockState state, BacterialExtractor_Entity entity) {
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

    private static boolean canCraft(BacterialExtractor_Entity entity) {
        ItemStack slot1 = entity.getStack(SLOT_1);
        ItemStack slot2 = entity.getStack(SLOT_2);
        ItemStack outputSlot = entity.getStack(OUTPUT_SLOT);
        
        // 检查输出槽是否为空，或者输出槽物品是否可以继续堆叠
        if (outputSlot.isEmpty() || outputSlot.getCount() < outputSlot.getMaxCount()) {
            // 检查输入物品
            if (slot1.isOf(Items.GLASS_BOTTLE) && slot2.isOf(ModFluids.SOIL_EXTRACT_BUCKET)) {
                // 检查是否有空间放置空桶
                boolean canPlaceEmptyBucket = false;
                // 如果第二个槽可以放入空桶
                if (slot2.getCount() == 1) {
                    // 如果只有一个桶，可以直接替换
                    canPlaceEmptyBucket = true;
                } else {
                    // 检查第二个槽是否有空间放入空桶
                    ItemStack currentStack = entity.getStack(SLOT_2);
                    ItemStack bucketStack = new ItemStack(Items.BUCKET);
                    
                    if (currentStack.isEmpty() || 
                        (ItemStack.canCombine(currentStack, bucketStack) && 
                         currentStack.getCount() < currentStack.getMaxCount())) {
                        canPlaceEmptyBucket = true;
                    } else {
                        // 检查输出槽是否有空间放入空桶
                        if (outputSlot.isEmpty() || 
                            (ItemStack.canCombine(outputSlot, bucketStack) && 
                             outputSlot.getCount() < outputSlot.getMaxCount())) {
                            canPlaceEmptyBucket = true;
                        }
                    }
                }
                
                if (!canPlaceEmptyBucket) {
                    return false;
                }
                
                // 根据选择的模式检查输出物品
                if (entity.selectedMode == 1) {
                    // 模式1：输出大肠杆菌提取液
                    return outputSlot.isEmpty() || 
                           (outputSlot.isOf(ModItems.E_COLI_EXTRACT) && 
                            outputSlot.getCount() < outputSlot.getMaxCount());
                } else if (entity.selectedMode == 2) {
                    // 模式2：输出抗生素分泌菌提取液
                    return outputSlot.isEmpty() || 
                           (outputSlot.isOf(ModItems.ANTIBIOTIC_BACTERIA_EXTRACT) && 
                            outputSlot.getCount() < outputSlot.getMaxCount());
                }
            }
        }
        
        return false;
    }

    private static void craftItem(BacterialExtractor_Entity entity) {
        ItemStack slot1 = entity.getStack(SLOT_1);
        ItemStack slot2 = entity.getStack(SLOT_2);
        ItemStack outputSlot = entity.getStack(OUTPUT_SLOT);
        
        if (slot1.isOf(Items.GLASS_BOTTLE) && slot2.isOf(ModFluids.SOIL_EXTRACT_BUCKET)) {
            // 消耗玻璃瓶
            slot1.decrement(1);
            
            // 消耗土壤提取液桶，返回空桶
            slot2.decrement(1);
            ItemStack emptyBucket = new ItemStack(Items.BUCKET);
            if (slot2.isEmpty()) {
                entity.setStack(SLOT_2, emptyBucket);
            } else {
                // 如果桶没有完全消耗(数量>1)，尝试将空桶添加到第二个槽
                // 如果第二个槽已满，则会尝试放入输出槽
                if (!entity.insertStack(SLOT_2, emptyBucket)) {
                    if (!entity.insertStack(OUTPUT_SLOT, emptyBucket)) {
                        // 如果无法放入输出槽，则尝试掉落在世界中
                        if (entity.world != null) {
                            ItemScatterer.spawn(entity.world, entity.getPos().getX(), entity.getPos().getY() + 1, entity.getPos().getZ(), emptyBucket);
                        }
                    }
                }
            }
            
            // 根据选择的模式生产物品
            if (entity.selectedMode == 1) {
                // 模式1：输出大肠杆菌提取液
                if (outputSlot.isEmpty()) {
                    entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.E_COLI_EXTRACT));
                } else {
                    outputSlot.increment(1);
                }
            } else if (entity.selectedMode == 2) {
                // 模式2：输出抗生素分泌菌提取液
                if (outputSlot.isEmpty()) {
                    entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.ANTIBIOTIC_BACTERIA_EXTRACT));
                } else {
                    outputSlot.increment(1);
                }
            }
        }
    }

    /**
     * 尝试插入物品到指定槽位
     * @param slot 目标槽位
     * @param stack 待插入的物品
     * @return 如果成功插入返回true，否则返回false
     */
    private boolean insertStack(int slot, ItemStack stack) {
        ItemStack currentStack = this.getStack(slot);
        
        if (currentStack.isEmpty()) {
            this.setStack(slot, stack.copy());
            stack.setCount(0);
            return true;
        } else if (ItemStack.canCombine(currentStack, stack)) {
            int spaceLeft = Math.min(this.getMaxCountPerStack(), currentStack.getMaxCount()) - currentStack.getCount();
            if (spaceLeft > 0) {
                int amountToAdd = Math.min(spaceLeft, stack.getCount());
                currentStack.increment(amountToAdd);
                stack.decrement(amountToAdd);
                return stack.isEmpty();
            }
        }
        
        return false;
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
        nbt.putInt("SelectedMode", selectedMode);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
        cookTime = nbt.getInt("CookTime");
        selectedMode = nbt.getInt("SelectedMode");
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.bacterial_extractor");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // 玩家打开界面时添加到交互列表
        addInteractingPlayer(player.getUuid());
        return new BacterialExtractor_ScreenHandler(syncId, playerInventory, this, player);
    }

    @Override
    public void markRemoved() {
        // 方块实体被移除时清空交互玩家列表
        interactingPlayers.clear();
        super.markRemoved();
    }

    public static final int SLOT_1 = 0;  // 输入槽位1 (玻璃瓶)
    public static final int SLOT_2 = 1;  // 输入槽位2 (土壤浸取液桶)
    public static final int OUTPUT_SLOT = 2;  // 输出槽位
} 