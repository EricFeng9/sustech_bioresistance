package sustech.bioresistance.complexBlocks;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.ItemScatterer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlasmidExtractor_Entity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    // 9个槽位：5个输入槽，4个输出槽
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);

    // 当前处理进度和总进度
    private int cookTime;
    private final int cookTimeTotal = 100; // 总处理时间，100游戏刻，5秒
    private int activeButton = 0; // 0表示没有按钮被按下

    // 跟踪正在交互的玩家
    private final List<UUID> interactingPlayers = new ArrayList<>();

    // 属性委托同步数据到客户端
    public final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(3) {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> cookTimeTotal;
                case 2 -> activeButton;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookTime = value;
                case 2 -> activeButton = value;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public PlasmidExtractor_Entity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.PlasmidExtractor, pos, state);
    }

    /**
     * 添加正在交互的玩家
     */
    public void addInteractingPlayer(UUID playerUUID) {
        if (!interactingPlayers.contains(playerUUID)) {
            interactingPlayers.add(playerUUID);
        }
    }

    /**
     * 移除不再交互的玩家
     */
    public void removeInteractingPlayer(UUID playerUUID) {
        interactingPlayers.remove(playerUUID);
    }

    /**
     * 检查是否有玩家正在与工作台交互
     */
    public boolean hasInteractingPlayers() {
        return !interactingPlayers.isEmpty();
    }

    /**
     * 设置当前活动的按钮
     */
    public void setActiveButton(int button) {
        if (button >= 0 && button <= 2) {
            this.activeButton = button;
            propertyDelegate.set(2, button);
            markDirty();
            
            // 如果在服务器端，通知客户端更新
            if (world != null && !world.isClient) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            }
        }
    }

    /**
     * 获取当前活动的按钮
     */
    public int getActiveButton() {
        return this.activeButton;
    }

    public static void tick(World world, BlockPos pos, BlockState state, PlasmidExtractor_Entity entity) {
        if (world.isClient) return;

        // 只有当有玩家正在交互时才进行处理
        if (entity.hasInteractingPlayers()) {
            int activeButton = entity.getActiveButton();
            
            // 如果按钮1被按下，立即处理Acidovorax citrulli培养基到DNA片段的转换
            if (activeButton == 1) {
                if (canProcessButton1(entity)) {
                    processButton1(entity);
                }
                // 无论是否成功处理，都立即重置按钮状态
                entity.setActiveButton(0);
                markDirty(world, pos, state);
            }
            // 如果按钮2被按下，立即处理DNA片段和大肠杆菌培养基到T6SS培养基的转换
            else if (activeButton == 2) {
                if (canProcessButton2(entity)) {
                    processButton2(entity);
                }
                // 无论是否成功处理，都立即重置按钮状态
                entity.setActiveButton(0);
                markDirty(world, pos, state);
            }
        }
    }

    // 判断按钮1操作是否可进行
    private static boolean canProcessButton1(PlasmidExtractor_Entity entity) {
        ItemStack inputSlot = entity.getStack(INPUT_SLOT_1);
        ItemStack outputSlot1 = entity.getStack(OUTPUT_SLOT_1);
        ItemStack outputSlot2 = entity.getStack(OUTPUT_SLOT_2);
        ItemStack outputSlot3 = entity.getStack(OUTPUT_SLOT_3);
        
        // 检查输入槽是否有Acidovorax citrulli培养基
        if (!inputSlot.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM)) {
            return false;
        }
        
        // 检查输出槽是否能放置DNA片段
        boolean canOutputDNA1 = outputSlot1.isEmpty() || 
            (outputSlot1.isOf(ModItems.DNA_SEGMENT_1) && 
             outputSlot1.getCount() < outputSlot1.getMaxCount());
             
        boolean canOutputDNA2 = outputSlot2.isEmpty() || 
            (outputSlot2.isOf(ModItems.DNA_SEGMENT_2) && 
             outputSlot2.getCount() < outputSlot2.getMaxCount());
             
        boolean canOutputDNA3 = outputSlot3.isEmpty() || 
            (outputSlot3.isOf(ModItems.DNA_SEGMENT_3) && 
             outputSlot3.getCount() < outputSlot3.getMaxCount());
             
        return canOutputDNA1 && canOutputDNA2 && canOutputDNA3;
    }
    
    // 处理按钮1的操作
    private static void processButton1(PlasmidExtractor_Entity entity) {
        ItemStack inputSlot = entity.getStack(INPUT_SLOT_1);
        
        // 消耗Acidovorax citrulli培养基
        inputSlot.decrement(1);
        
        // 输出DNA片段1到输出槽1
        ItemStack outputSlot1 = entity.getStack(OUTPUT_SLOT_1);
        if (outputSlot1.isEmpty()) {
            entity.setStack(OUTPUT_SLOT_1, new ItemStack(ModItems.DNA_SEGMENT_1));
        } else {
            outputSlot1.increment(1);
        }
        
        // 输出DNA片段2到输出槽2
        ItemStack outputSlot2 = entity.getStack(OUTPUT_SLOT_2);
        if (outputSlot2.isEmpty()) {
            entity.setStack(OUTPUT_SLOT_2, new ItemStack(ModItems.DNA_SEGMENT_2));
        } else {
            outputSlot2.increment(1);
        }
        
        // 输出DNA片段3到输出槽3
        ItemStack outputSlot3 = entity.getStack(OUTPUT_SLOT_3);
        if (outputSlot3.isEmpty()) {
            entity.setStack(OUTPUT_SLOT_3, new ItemStack(ModItems.DNA_SEGMENT_3));
        } else {
            outputSlot3.increment(1);
        }
    }
    
    // 判断按钮2操作是否可进行
    private static boolean canProcessButton2(PlasmidExtractor_Entity entity) {
        ItemStack ecoli = entity.getStack(INPUT_SLOT_5);
        ItemStack dna1 = entity.getStack(INPUT_SLOT_2);
        ItemStack dna2 = entity.getStack(INPUT_SLOT_3);
        ItemStack dna3 = entity.getStack(INPUT_SLOT_4);
        ItemStack outputSlot = entity.getStack(OUTPUT_SLOT_4);
        
        // 检查所有输入槽是否有对应物品
        if (!ecoli.isOf(ModItems.E_COLI_MEDIUM) ||
            !dna1.isOf(ModItems.DNA_SEGMENT_1) ||
            !dna2.isOf(ModItems.DNA_SEGMENT_2) ||
            !dna3.isOf(ModItems.DNA_SEGMENT_3)) {
            return false;
        }
        
        // 检查输出槽是否能放置T6SS培养基
        return outputSlot.isEmpty() || 
            (outputSlot.isOf(ModItems.E_COLI_T6SS_MEDIUM) && 
             outputSlot.getCount() < outputSlot.getMaxCount());
    }
    
    // 处理按钮2的操作
    private static void processButton2(PlasmidExtractor_Entity entity) {
        ItemStack ecoli = entity.getStack(INPUT_SLOT_5);
        ItemStack dna1 = entity.getStack(INPUT_SLOT_2);
        ItemStack dna2 = entity.getStack(INPUT_SLOT_3);
        ItemStack dna3 = entity.getStack(INPUT_SLOT_4);
        
        // 消耗大肠杆菌培养基
        ecoli.decrement(1);
        
        // 消耗三个DNA片段
        dna1.decrement(1);
        dna2.decrement(1);
        dna3.decrement(1);
        
        // 输出装配T6SS的大肠杆菌培养基
        ItemStack outputSlot = entity.getStack(OUTPUT_SLOT_4);
        if (outputSlot.isEmpty()) {
            entity.setStack(OUTPUT_SLOT_4, new ItemStack(ModItems.E_COLI_T6SS_MEDIUM));
        } else {
            outputSlot.increment(1);
        }
    }

    // 槽位常量定义
    public static final int INPUT_SLOT_1 = 0; // Acidovorax citrulli培养基输入槽
    public static final int INPUT_SLOT_2 = 1; // DNA片段1输入槽
    public static final int INPUT_SLOT_3 = 2; // DNA片段2输入槽
    public static final int INPUT_SLOT_4 = 3; // DNA片段3输入槽
    public static final int INPUT_SLOT_5 = 4; // 大肠杆菌培养基输入槽
    public static final int OUTPUT_SLOT_1 = 5; // DNA片段1输出槽
    public static final int OUTPUT_SLOT_2 = 6; // DNA片段2输出槽
    public static final int OUTPUT_SLOT_3 = 7; // DNA片段3输出槽
    public static final int OUTPUT_SLOT_4 = 8; // T6SS大肠杆菌输出槽

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
        nbt.putInt("cookTime", cookTime);
        nbt.putInt("activeButton", activeButton);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
        cookTime = nbt.getInt("cookTime");
        activeButton = nbt.getInt("activeButton");
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.bio-resistance.plasmid_extractor");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // 添加玩家到交互列表
        addInteractingPlayer(player.getUuid());
        return new PlasmidExtractor_ScreenHandler(syncId, playerInventory, this);
    }

    // 当方块被移除时清理资源
    @Override
    public void markRemoved() {
        super.markRemoved();
        // 清除所有交互的玩家
        interactingPlayers.clear();
    }
} 