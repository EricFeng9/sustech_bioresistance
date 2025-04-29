package sustech.bioresistance.complexBlocks;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import sustech.bioresistance.ModFluids;
import sustech.bioresistance.ModItems;
import sustech.bioresistance.ModScreenHandlers;

public class CleanTable_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public CleanTable_ScreenHandler(int syncId, PlayerInventory playerInventory, CleanTable_Entity blockEntity) {
        super(ModScreenHandlers.CLEAN_TABLE_SCREEN_HANDLER, syncId);
        this.inventory = blockEntity;
        this.propertyDelegate = blockEntity.propertyDelegate;
        this.addProperties(propertyDelegate); // 注册属性到客户端以同步数据

        // 添加槽位（保持原坐标不变�?
        // 石油桶槽�? (0) - 原水桶槽坐标
        this.addSlot(new Slot(blockEntity, CleanTable_Entity.OIL_SLOT, 80, 11) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModFluids.OIL_BUCKET);
            }
        });

        // 输入槽位 (1)
        this.addSlot(new Slot(blockEntity, CleanTable_Entity.INPUT_SLOT, 48, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.MEDIUM_STERILIZED); // 只允许放入培养基(消毒后的)
            }
        });

        // 输出槽位 (2) - 原输出槽坐标
        this.addSlot(new Slot(blockEntity, CleanTable_Entity.OUTPUT_SLOT, 108, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不可放入物�?
            }
        });

        // 添加玩家物品栏（保持原坐标不变）
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 快捷栏（保持原坐标不变）
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    // 获取制作进度（箭头动画）
    public int getCookProgress() {
        int currentTime = propertyDelegate.get(0); // 当前进度
        int totalTime = propertyDelegate.get(1);    // 总时�?
        return totalTime != 0 ? (currentTime * 24) / totalTime : 0; // 24像素宽度
    }

    // 快速移动物品逻辑（Shift+点击�?
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // 从机器槽位移到玩家背�?
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 从玩家背包移到机器槽�?
            else {
                // 尝试放入石油桶槽
                if (originalStack.isOf(ModFluids.OIL_BUCKET)) {
                    if (!this.insertItem(originalStack, CleanTable_Entity.OIL_SLOT, CleanTable_Entity.OIL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 尝试放入输入�?
                else if (originalStack.isOf(ModItems.MEDIUM_STERILIZED)) {
                    if (!this.insertItem(originalStack, CleanTable_Entity.INPUT_SLOT, CleanTable_Entity.INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 其他情况移到主背�?
                else if (invSlot < this.inventory.size() + 27) { // 主背�? �? 快捷�?
                    if (!this.insertItem(originalStack, this.inventory.size() + 27, this.inventory.size() + 36, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 快捷�? �? 主背�?
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