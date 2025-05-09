package sustech.bioresistance;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

public class ModWorldGen {
    public static void addRatSpawn() {
        BiomeModifications.addSpawn(
                BiomeSelectors.foundInOverworld(),
                        //.and(BiomeSelectors.excludeByTag(BiomeTags.IS_OCEAN))
                        //.and(BiomeSelectors.excludeByTag(BiomeTags.IS_DESERT)),
                SpawnGroup.MONSTER,
                ModEntities.RAT,
                50, // 权重
                1,  // 最小数量
                3   // 最大数量
        );

        // 特定结构生成
        SpawnRestriction.register(ModEntities.RAT, SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, RatEntity::canSpawnInDark);
    }
}