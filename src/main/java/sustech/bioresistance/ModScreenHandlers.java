package sustech.bioresistance;


import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sustech.bioresistance.complexBlocks.Autoclave_Entity;
import sustech.bioresistance.complexBlocks.Autoclave_ScreenHandler;
import sustech.bioresistance.complexBlocks.Bio_Fridge_Entity;
import sustech.bioresistance.complexBlocks.Bio_Fridge_ScreenHandler;
import sustech.bioresistance.complexBlocks.CleanTable_Entity;
import sustech.bioresistance.complexBlocks.CleanTable_ScreenHandler;

/**
 * 这里统一管理和注? ScreenHandler
 */
public class ModScreenHandlers {
    public static final ScreenHandlerType<Bio_Fridge_ScreenHandler> BIO_FRIDGE_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerExtended(
                    new Identifier(Bioresistance.MOD_ID, "bio_fridge_screen_handler"),
                    (syncId, playerInventory, buf) -> {
                        // �? PacketByteBuf 中读取方块坐�?
                        BlockPos pos = buf.readBlockPos();
                        // 获取客户端世界中指定坐标�? BlockEntity
                        World world = playerInventory.player.getWorld();
                        BlockEntity blockEntity = world.getBlockEntity(pos);

                        // 如果对应的方块确实是 BioFridgeBlockEntity，就用它�? inventory
                        if (blockEntity instanceof Bio_Fridge_Entity fridgeEntity) {
                            return new Bio_Fridge_ScreenHandler(syncId, playerInventory, fridgeEntity);
                        }

                        // 如果没找到，返回一个备用的 SimpleInventory 9�?
                        return new Bio_Fridge_ScreenHandler(syncId, playerInventory, new SimpleInventory(9));
                        
                        }
                );
    public static final ScreenHandlerType<Autoclave_ScreenHandler> AUTOCLAVE_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerExtended(
                    new Identifier(Bioresistance.MOD_ID, "autoclave"),
                    (syncId, playerInventory, buf) ->
                            new Autoclave_ScreenHandler(
                                    syncId,
                                    playerInventory,
                                    (Autoclave_Entity) playerInventory.player.getWorld().getBlockEntity(buf.readBlockPos())
                            )
            );
    public static final ScreenHandlerType<CleanTable_ScreenHandler> CLEAN_TABLE_SCREEN_HANDLER =
            ScreenHandlerRegistry.registerExtended(
                    new Identifier(Bioresistance.MOD_ID, "clean_table"),
                    (syncId, playerInventory, buf) ->
                            new CleanTable_ScreenHandler(syncId, playerInventory, (CleanTable_Entity) playerInventory.player.getWorld().getBlockEntity(buf.readBlockPos()))
            );

    public static void registerAll() {
    }
}
