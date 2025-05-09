package sustech.bioresistance;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;
import sustech.bioresistance.entities.RatEntity;

public class ModWorldGen {
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
}