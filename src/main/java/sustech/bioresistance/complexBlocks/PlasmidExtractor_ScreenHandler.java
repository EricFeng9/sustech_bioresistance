package sustech.bioresistance.complexBlocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import sustech.bioresistance.ModItems;
import sustech.bioresistance.ModScreenHandlers;

public class PlasmidExtractor_ScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    private final PlasmidExtractor_Entity blockEntity;
    private final PlayerEntity player;

    public PlasmidExtractor_ScreenHandler(int syncId, PlayerInventory playerInventory, PlasmidExtractor_Entity blockEntity) {
        this(syncId, playerInventory, blockEntity, playerInventory.player);
    }

    public PlasmidExtractor_ScreenHandler(int syncId, PlayerInventory playerInventory, PlasmidExtractor_Entity blockEntity, PlayerEntity player) {
        super(ModScreenHandlers.PLASMID_EXTRACTOR_SCREEN_HANDLER, syncId);
        this.inventory = blockEntity;
        this.propertyDelegate = blockEntity.propertyDelegate;
        this.blockEntity = blockEntity;
        this.player = player;
        this.addProperties(propertyDelegate); // 注册属性到客户端以同步数据

        // 添加输入槽位
        // 输入槽位1 - Acidovorax citrulli培养基
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.INPUT_SLOT_1, 70, 25) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM);
            }
        });

        // 输入槽位2 - DNA片段1
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.INPUT_SLOT_2, 125, 16) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.DNA_SEGMENT_1);
            }
        });

        // 输入槽位3 - DNA片段2
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.INPUT_SLOT_3, 125, 39) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.DNA_SEGMENT_2);
            }
        });

        // 输入槽位4 - DNA片段3
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.INPUT_SLOT_4, 125, 62) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.DNA_SEGMENT_3);
            }
        });

        // 输入槽位5 - 大肠杆菌培养基
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.INPUT_SLOT_5, 175, 25) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(ModItems.E_COLI_MEDIUM);
            }
        });

        // 添加输出槽位
        // 输出槽位1 - DNA片段1
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.OUTPUT_SLOT_1, 22, 16) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不可放入物品
            }
        });

        // 输出槽位2 - DNA片段2
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.OUTPUT_SLOT_2, 22, 39) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不可放入物品
            }
        });

        // 输出槽位3 - DNA片段3
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.OUTPUT_SLOT_3, 22, 62) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不可放入物品
            }
        });

        // 输出槽位4 - T6SS大肠杆菌培养基
        this.addSlot(new Slot(blockEntity, PlasmidExtractor_Entity.OUTPUT_SLOT_4, 228, 34) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false; // 输出槽不可放入物品
            }
        });

        // 添加玩家物品栏
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 51 + j * 18, 83 + i * 18));
            }
        }

        // 快捷栏
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 51 + i * 18, 141));
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        // 玩家关闭界面时，从交互列表中移除
        this.blockEntity.removeInteractingPlayer(player.getUuid());
    }

    // 获取制作进度
    public int getCookProgress() {
        int currentTime = propertyDelegate.get(0); // 当前进度
        int totalTime = propertyDelegate.get(1);   // 总时间
        return totalTime != 0 ? (currentTime * 24) / totalTime : 0; // 24像素宽度
    }

    // 获取当前活动的按钮
    public int getActiveButton() {
        return propertyDelegate.get(2);
    }

    // 设置活动按钮
    public void setActiveButton(int button) {
        if (button >= 0 && button <= 2) {
            propertyDelegate.set(2, button);
            // 确保服务器端的方块实体也更新了按钮状态
            if (!player.getWorld().isClient()) {
                this.blockEntity.setActiveButton(button);
            }
        }
    }

    // 处理从客户端发送的按钮点击事件
    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 1 || id == 2) {
            setActiveButton(id);
            return true;
        }
        return super.onButtonClick(player, id);
    }

    // 检查点击是否在按钮范围内
    public boolean isClickInButton1(int x, int y) {
        return x >= 64 && x < 64 + 29 && y >= 47 && y < 47 + 11;
    }

    public boolean isClickInButton2(int x, int y) {
        return x >= 168 && x < 168 + 29 && y >= 47 && y < 47 + 11;
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
                
                // 尝试放入对应的输入槽位
                if (stackCopy.isOf(ModItems.ACIDOVORAX_CITRULLI_MEDIUM)) {
                    if (!this.insertItem(originalStack, PlasmidExtractor_Entity.INPUT_SLOT_1, PlasmidExtractor_Entity.INPUT_SLOT_1 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (stackCopy.isOf(ModItems.DNA_SEGMENT_1)) {
                    if (!this.insertItem(originalStack, PlasmidExtractor_Entity.INPUT_SLOT_2, PlasmidExtractor_Entity.INPUT_SLOT_2 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (stackCopy.isOf(ModItems.DNA_SEGMENT_2)) {
                    if (!this.insertItem(originalStack, PlasmidExtractor_Entity.INPUT_SLOT_3, PlasmidExtractor_Entity.INPUT_SLOT_3 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (stackCopy.isOf(ModItems.DNA_SEGMENT_3)) {
                    if (!this.insertItem(originalStack, PlasmidExtractor_Entity.INPUT_SLOT_4, PlasmidExtractor_Entity.INPUT_SLOT_4 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (stackCopy.isOf(ModItems.E_COLI_MEDIUM)) {
                    if (!this.insertItem(originalStack, PlasmidExtractor_Entity.INPUT_SLOT_5, PlasmidExtractor_Entity.INPUT_SLOT_5 + 1, false)) {
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