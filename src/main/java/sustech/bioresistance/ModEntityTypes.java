package sustech.bioresistance;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sustech.bioresistance.complexBlocks.Autoclave_Entity;
import sustech.bioresistance.complexBlocks.BacterialExtractor_Entity;
import sustech.bioresistance.complexBlocks.Bio_Fridge_Entity;
import sustech.bioresistance.complexBlocks.CleanTable_Entity;
import sustech.bioresistance.complexBlocks.PlasmidExtractor_Entity;

public class ModEntityTypes {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("tutorial", path), blockEntityType);
    }

    public static final BlockEntityType<Bio_Fridge_Entity> Bio_Fridge = register(
            "bio_fridge",
            // 对于 1.21.2 及以上的版本，
            // 请将 `BlockEntityType.Builder` 替换为 `FabricBlockEntityTypeBuilder`。
            BlockEntityType.Builder.create(Bio_Fridge_Entity::new, ModBlocks.Bio_Fridge).build()
    );
    public static final BlockEntityType<Autoclave_Entity> Autoclave = register(
            "auto_clave",
            // 对于 1.21.2 及以上的版本，
            // 请将 `BlockEntityType.Builder` 替换为 `FabricBlockEntityTypeBuilder`。
            BlockEntityType.Builder.create(Autoclave_Entity::new, ModBlocks.Autoclave).build()
    );
    public static final BlockEntityType<CleanTable_Entity> CleanTable = register(
            "clean_table",
            BlockEntityType.Builder.create(CleanTable_Entity::new, ModBlocks.CleanTable).build()
    );
    public static final BlockEntityType<BacterialExtractor_Entity> BacterialExtractor = register(
            "bacterial_extractor",
            BlockEntityType.Builder.create(BacterialExtractor_Entity::new, ModBlocks.BacterialExtractor).build()
    );
    // 由于ModBlocks.PlasmidExtractor可能尚未初始化，我们在initialize()方法中延迟注册
    public static BlockEntityType<PlasmidExtractor_Entity> PlasmidExtractor;

    public static void initialize() {
        // 在这里延迟初始化质粒提取器的BlockEntityType
        PlasmidExtractor = register(
            "plasmid_extractor",
            BlockEntityType.Builder.create(PlasmidExtractor_Entity::new, ModBlocks.PlasmidExtractor).build()
        );
    }
}
