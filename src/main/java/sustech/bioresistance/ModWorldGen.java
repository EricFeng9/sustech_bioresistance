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
    // 添加更多村庄类型的标签以确保所有村庄都能生成老鼠
    public static final TagKey<Biome> VILLAGE_DESERT = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "has_structure/village_desert"));
    public static final TagKey<Biome> VILLAGE_SAVANNA = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "has_structure/village_savanna"));
    public static final TagKey<Biome> VILLAGE_SNOWY = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "has_structure/village_snowy"));
    public static final TagKey<Biome> VILLAGE_TAIGA = TagKey.of(RegistryKeys.BIOME, new Identifier("minecraft", "has_structure/village_taiga"));

    public static void addRatSpawn() {
        // 在主世界所有生物群系增加老鼠生成概率（提高权重）
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                30, // 降低权重，原来是40
                1,   // 最小数量
                3    // 最大数量3
        );

        // 在村庄中必定生成大量老鼠
        // 平原村庄
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(VILLAGE_PLAINS),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                200, // 在村庄中权重更高
                2,   // 最小数量2
                3    // 最大数量3
        );
        
        // 沙漠村庄有老鼠概率更小
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(VILLAGE_DESERT),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                100,
                2,
                2
        );
        
        // 热带草原村庄
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(VILLAGE_SAVANNA),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                200,
                2,
                3
        );
        
        // 雪地村庄不会生成老鼠
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(VILLAGE_SNOWY),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                0,
                0,
                0
        );
        
        // 针叶林村庄
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(VILLAGE_TAIGA),
                SpawnGroup.CREATURE,
                ModEntities.RAT,
                200,
                2,
                6
        );

        // 为村庄中的老鼠设置特殊的生成条件，不受光照限制
        SpawnRestriction.register(
                ModEntities.RAT, 
                SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, 
                (type, world, reason, pos, random) -> {
                    // 检查是否在村庄中
                    if (world.getBiome(pos).isIn(VILLAGE_PLAINS) || 
                        world.getBiome(pos).isIn(VILLAGE_DESERT) || 
                        world.getBiome(pos).isIn(VILLAGE_SAVANNA) || 
                        world.getBiome(pos).isIn(VILLAGE_SNOWY) || 
                        world.getBiome(pos).isIn(VILLAGE_TAIGA)) {
                        // 在村庄中使用宽松的生成条件
                        return RatEntity.canSpawnInVillage(type, world, reason, pos, random);
                    } else {
                        // 在其他地方使用正常的生成条件
                        return RatEntity.canSpawnInDark(type, world, reason, pos, random);
                    }
                }
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