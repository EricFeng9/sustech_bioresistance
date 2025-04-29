package sustech.bioresistance.complexBlocks;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import sustech.bioresistance.ImplementedInventory;
import sustech.bioresistance.ModEntityTypes;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;


public class Bio_Fridge_Entity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    public Bio_Fridge_Entity(BlockPos pos, BlockState state) {
        super(ModEntityTypes.Bio_Fridge, pos, state);
    }

    /** 这里定义物品栏大小，比如 9 个槽�? */
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
    // ===================== 实现存储功能 =====================

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.items;
    }

    /**
     * @param player
     * @return true 如果玩家可以使用物品栏，否则�? false。i
     */
    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return pos.isWithinDistance(player.getBlockPos(), 4.5); // 玩家距离方块 4.5 格内可操�?
    }

    /**
     * 在写�? NBT 时，把物品存进去
     */
    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.items);
    }

    /**
     * 在读�? NBT 时，把物品读出来
     */
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.items);
    }
    //------


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
        return items.stream().allMatch(ItemStack::isEmpty);
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
     * @param stack 替换后新的物品堆。如果堆对于此物品栏过大（{@link Inventory#getMaxCountPerStack()}），则压缩为物品栏的最大数量�?
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



    @Override
    public void onOpen(PlayerEntity player) {
        ImplementedInventory.super.onOpen(player);
    }

    @Override
    public void onClose(PlayerEntity player) {
        ImplementedInventory.super.onClose(player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return ImplementedInventory.super.isValid(slot, stack);
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return ImplementedInventory.super.canTransferTo(hopperInventory, slot, stack);
    }

    @Override
    public int count(Item item) {
        return ImplementedInventory.super.count(item);
    }

    @Override
    public boolean containsAny(Set<Item> items) {
        return ImplementedInventory.super.containsAny(items);
    }

    @Override
    public boolean containsAny(Predicate<ItemStack> predicate) {
        return ImplementedInventory.super.containsAny(predicate);
    }


    @Override
    public @Nullable Object getRenderData() {
        return super.getRenderData();
    }

    /**
     * 这个名字会显示在界面顶部（如 “Bio Fridge”）
     */
    @Override
    public Text getDisplayName() {
        return Text.translatable("bioresistance.bio_fridge");
    }



    /**
     * 当玩家打开该方块时，创建并返回 ScreenHandler
     * @param syncId  同步 ID
     * @param playerInventory   玩家背包
     * @param player  玩家本人
     */
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new Bio_Fridge_ScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        // 把方块实体的 pos 序列化进 PacketByteBuf
        buf.writeBlockPos(this.pos);
    }


    @Override
    public boolean shouldCloseCurrentScreen() {
        return ExtendedScreenHandlerFactory.super.shouldCloseCurrentScreen();
    }



    @Override
    public <A> A getAttachedOrThrow(AttachmentType<A> type) {
        return super.getAttachedOrThrow(type);
    }

    @Override
    public <A> A getAttachedOrSet(AttachmentType<A> type, A defaultValue) {
        return super.getAttachedOrSet(type, defaultValue);
    }

    @Override
    public <A> A getAttachedOrCreate(AttachmentType<A> type, Supplier<A> initializer) {
        return super.getAttachedOrCreate(type, initializer);
    }

    @Override
    public <A> A getAttachedOrCreate(AttachmentType<A> type) {
        return super.getAttachedOrCreate(type);
    }

    @Override
    public <A> A getAttachedOrElse(AttachmentType<A> type, @Nullable A defaultValue) {
        return super.getAttachedOrElse(type, defaultValue);
    }

    @Override
    public <A> A getAttachedOrGet(AttachmentType<A> type, Supplier<A> defaultValue) {
        return super.getAttachedOrGet(type, defaultValue);
    }

    @Override
    public <A> @Nullable A setAttached(AttachmentType<A> type, @Nullable A value) {
        return super.setAttached(type, value);
    }

    @Override
    public boolean hasAttached(AttachmentType<?> type) {
        return super.hasAttached(type);
    }

    @Override
    public <A> @Nullable A removeAttached(AttachmentType<A> type) {
        return super.removeAttached(type);
    }

    @Override
    public <A> @Nullable A modifyAttached(AttachmentType<A> type, UnaryOperator<A> modifier) {
        return super.modifyAttached(type, modifier);
    }

}
