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

public class CleanTable_Entity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private int cookTime; // 当前处理进度
    private final int cookTimeTotal = 200; // 总处理时�?

    // 属性委托同步数据到客户�?
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

    public static void tick(World world, BlockPos pos, BlockState state, CleanTable_Entity entity) {
        boolean hasOil = !entity.getStack(OIL_SLOT).isEmpty()
                && entity.getStack(OIL_SLOT).isOf(ModFluids.OIL_BUCKET);
        boolean canCraft = canCraft(entity);

        if (hasOil && canCraft) {
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
    }

    private static boolean canCraft(CleanTable_Entity entity) {
        ItemStack input = entity.getStack(INPUT_SLOT);
        ItemStack output = entity.getStack(OUTPUT_SLOT);
        return input.isOf(ModItems.MEDIUM_STERILIZED)
                && (output.isEmpty());
    }

    private static void craftItem(CleanTable_Entity entity) {
        entity.getStack(INPUT_SLOT).decrement(1);
        ItemStack output = entity.getStack(OUTPUT_SLOT);
        if (output.isEmpty()) {

            int num = (int)(Math.random()*100) + 1;//生成一个[1�?100]之间的随机数�?
            if (num<=50){
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.EXPLANATORY_LIQUID_B));
            }
            else {
                entity.setStack(OUTPUT_SLOT, new ItemStack(ModItems.EXPLANATORY_LIQUID_BP));
            }

        } else {
            output.increment(1);
        }
        //消耗石油桶（可选，如果不消耗则移除下面两行�?
        ItemStack oil = entity.getStack(OIL_SLOT);
        if (oil.isOf(ModFluids.OIL_BUCKET)) {
            oil.decrement(1);
            entity.setStack(OIL_SLOT, new ItemStack(Items.BUCKET)); // 替换为空�?
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
        return Text.translatable("container.bioresistance.clean_table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CleanTable_ScreenHandler(syncId, playerInventory, this);
    }

    public static final int OIL_SLOT = 0;    // 石油桶槽�?
    public static final int INPUT_SLOT = 1;  // 输入槽位
    public static final int OUTPUT_SLOT = 2;  // 输出槽位

    // Inventory 方法继承�? ImplementedInventory，无需重复实现
}