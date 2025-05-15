package sustech.bioresistance;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.structure.Structure;
import sustech.bioresistance.entities.RatEntity;

public class ModWorldGen {
    // 注册结构的ID
    public static final Identifier CLINIC_ID = new Identifier("bio-resistance", "clinic");
    
    // 创建平原村庄生物群系标签
    public static final TagKey<Biome> VILLAGE_PLAINS = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "has_structure/village_plains"));

    public static void addRatSpawn() {
        // 在主世界所有生物群系生成老鼠（普通权重）
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                40, // 普通权重
                1,  // 最小数量
                3   // 最大数量
        );

        // 设置生成限制
        SpawnRestriction.register(
                ModEntities.RAT, 
                SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, 
                RatEntity::canSpawnInDark
        );
    }
    
    // 添加诊所结构到生物群系
    public static void addClinicStructure() {
        // 我们不需要在代码中注册结构，因为它是通过数据包完成的
        // 现在只需要记录，结构是通过JSON配置文件定义的
        Bioresistance.LOGGER.info("诊所结构已通过数据包注册");
    }
    
    // 初始化所有世界生成内容
    public static void initialize() {
        addRatSpawn();
        addClinicStructure();
    }
}