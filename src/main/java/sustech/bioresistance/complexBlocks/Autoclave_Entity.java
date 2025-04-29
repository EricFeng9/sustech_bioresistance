package sustech.bioresistance.complexBlocks;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
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

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class Autoclave_Entity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(4, ItemStack.EMPTY);

    public int burnTime;
    public int fuelTime;
    private int cookTime; // 当前处理进度
    private int cookTimeTotal = 200; // 每个产物需要的总时间（例如200 tick = 10秒）
    public Autoclave_Entity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.Autoclave, pos, state);
    }

    protected final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(2) {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> fuelTime;
                case 2 -> cookTime;
                case 3 -> cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> fuelTime = value;
                case 2 -> cookTime= value;
                case 3 -> cookTimeTotal= value;
            }
        }

        @Override
        public int size() {
            return 4;
        }
    };; // 跟踪燃烧时间和总燃烧时�?
    public static void tick(World world, BlockPos pos, BlockState state, Autoclave_Entity entity) {
        boolean hasWater = !entity.getStack(WATER_SLOT).isEmpty()
                && entity.getStack(WATER_SLOT).isOf(Items.WATER_BUCKET);
        boolean canCraft = canCraft(entity);

        if (hasWater && canCraft) {
            if (entity.burnTime > 0) {
                entity.burnTime--;
                entity.cookTime++;
                if (entity.cookTime >= entity.cookTimeTotal) {
                    craftItem(entity);
                    entity.cookTime = 0; // 重置制作进度
                }
            } else {
                // 尝试添加新燃�?
                ItemStack fuel = entity.getStack(FUEL_SLOT);
                if (fuel.isEmpty()) return;
                entity.fuelTime = getFuelTime(fuel);
                entity.burnTime = entity.fuelTime;
                if (entity.burnTime > 0) {
                    fuel.decrement(1);
                }
            }
            markDirty(world, pos, state);
        } else {
            entity.cookTime = 0; // 条件不满足时重置进度
            if (entity.burnTime > 0) {
                entity.burnTime = 0;
                markDirty(world, pos, state);
            }
        }
    }

    private static boolean canCraft(Autoclave_Entity entity) {
        ItemStack input = entity.getStack(INPUT_SLOT);
        ItemStack output = entity.getStack(OUTPUT_SLOT);
        return input.isOf(ModItems.MEDIUM)
                && (output.isEmpty() || (output.isOf(ModItems.MEDIUM_STERILIZED)
                && output.getCount() < output.getMaxCount()));
    }

    private static void craftItem(Autoclave_Entity entity) {
        entity.getStack(INPUT_SLOT).decrement(1);
        ItemStack output = entity.getStack(OUTPUT_SLOT);
        if (output.isEmpty()) {
            entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.MEDIUM_STERILIZED));
        } else {
            output.increment(1);
        }
    }

    private static int getFuelTime(ItemStack fuel) {
        return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(fuel.getItem(), 0);
    }

    // 实现 Inventory 方法
    // ... 实现 size(), getStack(), setStack(), clear() 等方�?

    //存储数据
    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);
        nbt.putInt("BurnTime", burnTime);
        nbt.putInt("FuelTime", fuelTime);
        nbt.putInt("CookTime", cookTime);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
        burnTime = nbt.getInt("BurnTime");
        fuelTime = nbt.getInt("FuelTime");
        cookTime = nbt.getInt("CookTime");
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.bioresistance.autoclave");
    }


    @Nullable
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Autoclave_ScreenHandler(syncId, playerInventory, this);
    }

    public static final int INPUT_SLOT = 0;
    public static final int WATER_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;


    /**
     * 从此物品栏中检索物品�?
     * 每次被调用时必须返回相同实例�?
     */
    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

    /**
     * 返回物品栏的大小�?
     */
    @Override
    public int size() {
        return items.size();
    }

    /**
     * 检查物品栏是否为空�?
     *
     * @return true，如果物品栏仅有一个空堆，否则为true�?
     */
    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 检索槽位中的物品�?
     *
     * @param slot
     */
    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    /**
     * 从物品栏槽位移除物品�?
     *
     * @param slot  从该槽位移除�?
     * @param count 需要移除的物品个数。如果槽位中的物品少于需要的，则将其全部取出�?
     */
    @Override
    public ItemStack removeStack(int slot, int count) {
        ItemStack stack = Inventories.splitStack(items, slot, count);
        markDirty();
        return stack;
    }

    /**
     * 从物品栏槽位移除所有物品�?
     *
     * @param slot 从该槽位移除�?
     */
    @Override
    public ItemStack removeStack(int slot) {
        ItemStack stack = Inventories.removeStack(items, slot);
        markDirty();
        return stack;
    }

    /**
     * 将物品栏槽位中的当前物品堆替换为提供的物品堆�?
     *
     * @param slot  替换该槽位的物品堆�?
     * @param stack 替换后新的物品堆。如果堆对于此物品栏过大，则压缩为物品栏的最大数量�?
     */
    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        markDirty();
    }

    /**
     * 清除物品栏�?
     */
    @Override
    public void clear() {
        items.clear();
        markDirty();
    }

    /**
     * @param player
     * @return true 如果玩家可以使用物品栏，否则�? false。i
     */
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return ImplementedInventory.super.canPlayerUse(player);
    }
}
