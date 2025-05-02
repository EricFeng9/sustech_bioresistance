package sustech.bioresistance.complexBlocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import sustech.bioresistance.ModFluids;
import sustech.bioresistance.ModScreenHandlers;

public class BacterialExtractor_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final BacterialExtractor_Entity blockEntity;
    private final PlayerEntity player;

    public BacterialExtractor_ScreenHandler(int syncId, PlayerInventory playerInventory, BacterialExtractor_Entity blockEntity) {
        this(syncId, playerInventory, blockEntity, playerInventory.player);
    }

    public BacterialExtractor_ScreenHandler(int syncId, PlayerInventory playerInventory, BacterialExtractor_Entity blockEntity, PlayerEntity player) {
        super(ModScreenHandlers.BACTERIAL_EXTRACTOR_SCREEN_HANDLER, syncId);
        this.inventory = blockEntity;
        this.propertyDelegate = blockEntity.propertyDelegate;
        this.blockEntity = blockEntity;
        this.player = player;
        this.addProperties(propertyDelegate); // 注册属性到客户端以同步数据

        // 添加槽位
        // 输入槽位1 - 玻璃瓶
        this.addSlot(new Slot(blockEntity, BacterialExtractor_Entity.SLOT_1, 38, 38) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.GLASS_BOTTLE);
            }
        });

        // 输入槽位2 - 土壤浸取液桶
        this.addSlot(new Slot(blockEntity, BacterialExtractor_Entity.SLOT_2, 63, 38) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModFluids.SOIL_EXTRACT_BUCKET);
            }
        });

        // 输出槽位
        this.addSlot(new Slot(blockEntity, BacterialExtractor_Entity.OUTPUT_SLOT, 123, 38) {
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

    // 获取当前选择的模式
    public int getSelectedMode() {
        return propertyDelegate.get(2);
    }

    // 设置选择的模式
    public void setSelectedMode(int mode) {
        if (mode == 1 || mode == 2) {
            propertyDelegate.set(2, mode);
            // 确保服务器端的方块实体也更新了模式
            if (!player.getWorld().isClient()) {
                this.blockEntity.setSelectedMode(mode);
            }
        }
    }

    // 处理从客户端发送的按钮点击事件
    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 1 || id == 2) {
            setSelectedMode(id);
            return true;
        }
        return super.onButtonClick(player, id);
    }

    // 检查点击是否在按钮范围内
    public boolean isClickInMode1Button(int x, int y) {
        return x >= 61 && x < 61 + 29 && y >= 20 && y < 20 + 11;
    }

    public boolean isClickInMode2Button(int x, int y) {
        return x >= 89 && x < 89 + 29 && y >= 20 && y < 20 + 11;
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
                
                // 尝试放入输入槽1 - 玻璃瓶
                if (stackCopy.isOf(Items.GLASS_BOTTLE)) {
                    if (!this.insertItem(originalStack, BacterialExtractor_Entity.SLOT_1, BacterialExtractor_Entity.SLOT_1 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 尝试放入输入槽2 - 土壤浸取液桶
                else if (stackCopy.isOf(ModFluids.SOIL_EXTRACT_BUCKET)) {
                    if (!this.insertItem(originalStack, BacterialExtractor_Entity.SLOT_2, BacterialExtractor_Entity.SLOT_2 + 1, false)) {
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