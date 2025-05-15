package sustech.bioresistance;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import sustech.bioresistance.complexBlocks.*;

public class ModBlocks {
    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        Identifier id = new Identifier(Bioresistance.MOD_ID, name);

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, blockItem);
        }
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.CUSTOM_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.add(ModBlocks.Bio_Fridge);
            itemGroup.add(ModBlocks.POLLUTION_BLOCK);
            itemGroup.add(ModBlocks.Agar_Block);
            itemGroup.add(ModBlocks.Autoclave);
            itemGroup.add(ModBlocks.CleanTable);
            itemGroup.add(ModBlocks.BacterialExtractor);
            itemGroup.add(ModBlocks.PlasmidExtractor);
        });


    }

    public static final Block Bio_Fridge = register(
            new Bio_Fridge(AbstractBlock.Settings.copy(Blocks.STONE)),
            "bio_fridge",
            true
            );
    public static final Block Autoclave = register(
            new Autoclave(AbstractBlock.Settings.copy(Blocks.STONE)),
            "autoclave",
            true
            );
    public static final Block CleanTable = register(
            new CleanTable(AbstractBlock.Settings.copy(Blocks.STONE)),
            "clean_table",
            true
            );
    public static final Block BacterialExtractor = register(
            new BacterialExtractor(AbstractBlock.Settings.copy(Blocks.STONE)),
            "bacterial_extractor",
            true
            );
    public static final Block PlasmidExtractor = register(
            new PlasmidExtractor(AbstractBlock.Settings.copy(Blocks.STONE)),
            "plasmid_extractor",
            true
            );

    public static final Block Agar_Block = register(
            new Agar_Block(AbstractBlock.Settings.copy(Blocks.SLIME_BLOCK).sounds(BlockSoundGroup.SLIME)),
            "agar_block",
            true
    );
    public static final Block Agar_Block_Yellow = register(
            new Agar_Block_Yellow(AbstractBlock.Settings.copy(Blocks.SLIME_BLOCK).sounds(BlockSoundGroup.SLIME)),
            "agar_block_yellow",
            true
    );

    public static final Block OCCUPIED_BLOCK = register(
            new OccupiedBlock(AbstractBlock.Settings.copy(Blocks.AIR)),
            "occupied_block",
            false
            );

    public static final Block POLLUTION_BLOCK = register(
            new Block(AbstractBlock.Settings.create().requiresTool().strength(3.0f, 3.0f).sounds(BlockSoundGroup.ROOTED_DIRT)),
            "pollution_block",
            true);
}
