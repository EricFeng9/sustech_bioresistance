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
import sustech.bioresistance.ModItems;
import sustech.bioresistance.ModScreenHandlers;

public class Autoclave_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public Autoclave_ScreenHandler(int syncId, PlayerInventory playerInventory, Autoclave_Entity blockEntity) {
        super(ModScreenHandlers.AUTOCLAVE_SCREEN_HANDLER, syncId);
        this.inventory = blockEntity;
        // 绑定属性委托（用于同步燃烧进度等）
        // 使用方块实体的属性委�?
        this.propertyDelegate = blockEntity.propertyDelegate;
        this.addProperties(propertyDelegate); // 关键：注册属性到客户�?

        // 添加槽位
        // 输入�? (0)
        this.addSlot(new Slot(blockEntity, Autoclave_Entity.INPUT_SLOT, 48, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.MEDIUM);
            }
        });

        // 水桶�? (1)
        this.addSlot(new Slot(blockEntity, Autoclave_Entity.WATER_SLOT, 80, 11) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.WATER_BUCKET);
            }
        });

        // 燃料�? (2)
        this.addSlot(new Slot(blockEntity, Autoclave_Entity.FUEL_SLOT, 80, 59) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return AbstractFurnaceBlockEntity.canUseAsFuel(stack);
            }
        });

        // 输出�? (3)
        this.addSlot(new Slot(blockEntity, Autoclave_Entity.OUTPUT_SLOT, 108, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不能放入物�?
            }
        });

        // 添加玩家物品�?
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 快捷�?
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
    // 获取燃烧进度（用�? GUI 绘制�?
    public int getBurnProgress() {
        int burnTime = propertyDelegate.get(0);
        int fuelTime = propertyDelegate.get(1);
        return fuelTime != 0 && burnTime != 0 ? burnTime * 13 / fuelTime : 0;
    }

    // 获取制作进度（箭头动画）
    public int getCookProgress() {
        return propertyDelegate.get(3)!=0 ?(propertyDelegate.get(2) * 24) / propertyDelegate.get(3):0; // 24像素宽度
        //(cookTime * 24) / cookTimeTotal; // 24像素宽度
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

}
