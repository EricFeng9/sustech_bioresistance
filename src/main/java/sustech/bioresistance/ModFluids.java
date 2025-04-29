package sustech.bioresistance;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import sustech.bioresistance.fluids.Oil_Fluid;
import sustech.bioresistance.fluids.Oil_Fluid_Block;


public class ModFluids {
    // -----------------------------
    // 1) 注册用到的 Identifier
    // -----------------------------
    public static final Identifier STILL_OIL_ID = new Identifier(Bioresistance.MOD_ID, "still_oil");
    public static final Identifier FLOWING_OIL_ID = new Identifier(Bioresistance.MOD_ID, "flowing_oil");
    public static final Identifier OIL_FLUID_BLOCK_ID = new Identifier(Bioresistance.MOD_ID, "oil_fluid_block");
    public static final Identifier OIL_BUCKET_ID = new Identifier(Bioresistance.MOD_ID, "oil_bucket");
    // -----------------------------
    // 2) 流体实例、流体方块、油桶
    // -----------------------------
    public static FlowableFluid STILL_OIL;
    public static FlowableFluid FLOWING_OIL;
    public static Block OIL_FLUID_BLOCK;
    public static Item OIL_BUCKET;

    public static void initialize() {
        // 2.1) 先实例化流体
        STILL_OIL = Registry.register(Registries.FLUID, STILL_OIL_ID, new Oil_Fluid.Oil_Fluid_Still());
        FLOWING_OIL = Registry.register(Registries.FLUID, FLOWING_OIL_ID, new Oil_Fluid.Oil_Fluid_Flowing());

        // 2.2) 创建并注册流体方块
        OIL_FLUID_BLOCK = Registry.register(Registries.BLOCK, OIL_FLUID_BLOCK_ID,
                new Oil_Fluid_Block(STILL_OIL, Block.Settings.create()//todo 这里有改动
                        .noCollision()    // 允许实体进入
                        .dropsNothing()   // 流体块被收集时不会掉落物品
                        .pistonBehavior(PistonBehavior.DESTROY)
                        .liquid()
                        .sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)
                ));


        // 2.3) 创建并注册油桶
        OIL_BUCKET = Registry.register(Registries.ITEM, OIL_BUCKET_ID,
                new BucketItem(STILL_OIL, new Item.Settings().maxCount(1)));
        //添加进物品栏
        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.CUSTOM_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModFluids.OIL_BUCKET);
        });


        // 提示：别忘了在 resources/lang/xx_xx.json 里写上翻译文本
        // 以及为 texture 准备对应的 sprite (atlases/...)。
    }
}
