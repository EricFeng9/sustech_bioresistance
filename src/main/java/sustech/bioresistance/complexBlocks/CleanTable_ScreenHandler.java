package sustech.bioresistance.complexBlocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import sustech.bioresistance.ModItems;
import sustech.bioresistance.ModScreenHandlers;

public class CleanTable_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final CleanTable_Entity blockEntity;
    private final PlayerEntity player;

    public CleanTable_ScreenHandler(int syncId, PlayerInventory playerInventory, CleanTable_Entity blockEntity) {
        this(syncId, playerInventory, blockEntity, playerInventory.player);
    }

    public CleanTable_ScreenHandler(int syncId, PlayerInventory playerInventory, CleanTable_Entity blockEntity, PlayerEntity player) {
        super(ModScreenHandlers.CLEAN_TABLE_SCREEN_HANDLER, syncId);
        this.inventory = blockEntity;
        this.propertyDelegate = blockEntity.propertyDelegate;
        this.blockEntity = blockEntity;
        this.player = player;
        this.addProperties(propertyDelegate); // 注册属性到客户端以同步数据

        // 添加槽位
        // 输入槽位1  - 允许放入注射器/水凝胶/胶囊/空培养基
        this.addSlot(new Slot(blockEntity, CleanTable_Entity.SLOT_1, 80, 11) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.SYRINGE) ||
                       stack.isOf(ModItems.HYDROGEL) ||
                       stack.isOf(ModItems.EMPTY_CAPSULE) ||
                       stack.isOf(ModItems.MEDIUM_STERILIZED);
            }
        });

        // 输入槽位2  - 允许放入各种培养基和提取液
        this.addSlot(new Slot(blockEntity, CleanTable_Entity.SLOT_2, 48, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.ANTIBIOTIC_BACTERIA_EXTRACT) ||
                       stack.isOf(ModItems.E_COLI_T6SS_MEDIUM) ||
                       stack.isOf(Items.MELON_SLICE) ||
                       stack.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM) ||
                       stack.isOf(ModItems.E_COLI_EXTRACT) ||
                       stack.isOf(ModItems.E_COLI_MEDIUM);
            }
        });

        // 输出槽位
        this.addSlot(new Slot(blockEntity, CleanTable_Entity.OUTPUT_SLOT, 108, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不可放入物品
            }
        });

        // 添加玩家物品栏
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 快捷栏
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        // 玩家关闭界面时，从交互列表中移除
        this.blockEntity.removeInteractingPlayer(player.getUuid());
    }

    // 获取制作进度（箭头动画）
    public int getCookProgress() {
        int currentTime = propertyDelegate.get(0); // 当前进度
        int totalTime = propertyDelegate.get(1);   // 总时间
        return totalTime != 0 ? (currentTime * 24) / totalTime : 0; // 24像素宽度
    }

    // 快速移动物品逻辑（Shift+点击）
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // 从机器槽位移到玩家背包
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 从玩家背包移到机器槽位
            else {
                ItemStack stackCopy = originalStack.copy();
                
                // 尝试放入输入槽1
                if (stackCopy.isOf(ModItems.SYRINGE) || 
                    stackCopy.isOf(ModItems.HYDROGEL) || 
                    stackCopy.isOf(ModItems.EMPTY_CAPSULE) || 
                    stackCopy.isOf(ModItems.MEDIUM_STERILIZED)) {
                    if (!this.insertItem(originalStack, CleanTable_Entity.SLOT_1, CleanTable_Entity.SLOT_1 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 尝试放入输入槽2
                else if (stackCopy.isOf(ModItems.ANTIBIOTIC_BACTERIA_EXTRACT) || 
                         stackCopy.isOf(ModItems.E_COLI_T6SS_MEDIUM) || 
                         stackCopy.isOf(Items.MELON_SLICE) || 
                         stackCopy.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM) || 
                         stackCopy.isOf(ModItems.E_COLI_EXTRACT) || 
                         stackCopy.isOf(ModItems.E_COLI_MEDIUM)) {
                    if (!this.insertItem(originalStack, CleanTable_Entity.SLOT_2, CleanTable_Entity.SLOT_2 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 其他情况移到主背包
                else if (invSlot < this.inventory.size() + 27) { // 主背包 -> 快捷栏
                    if (!this.insertItem(originalStack, this.inventory.size() + 27, this.inventory.size() + 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 快捷栏 -> 主背包
                else if (!this.insertItem(originalStack, this.inventory.size(), this.inventory.size() + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
}