package sustech.bioresistance.complexBlocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import sustech.bioresistance.ModScreenHandlers;

public class Bio_Fridge_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    /**
     * 构造器
     * @param syncId 一个屏幕同�? ID（服务器�?+客户端对应）
     * @param playerInventory 玩家背包
     * @param inventory 方块实体的物品栏
     */
    public Bio_Fridge_ScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.BIO_FRIDGE_SCREEN_HANDLER, syncId);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // 这里我们默认 9 格位
        int fridgeSize = inventory.size(); // 9
        // 将方块实体的槽位添加�? ScreenHandler
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(inventory, 3*i+j,
                        62 + j * 18, 16 + i * 18));
            }
        }

        // 接下来把玩家背包 “主物品栏�? 添加进来 (3行�?9�?)
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9,
                        8 + j * 18, 84 + i * 18));
            }
        }

        // 再把玩家的快捷栏(Hotbar)添加进来 (1行�?9�?)
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }


    // 处理 Shift+点击快速移动物�?
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    // 玩家是否可以交互
    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

}
