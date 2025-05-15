package sustech.bioresistance;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.entities.RatEntity;
import sustech.bioresistance.entities.DoctorEntity;

public class ModEntities {
    public static final EntityType<RatEntity> RAT = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Bioresistance.MOD_ID, "rat"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RatEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                    .build()
    );

    public static final EntityType<DoctorEntity> DOCTOR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Bioresistance.MOD_ID, "doctor"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, DoctorEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                    .build()
    );

    public static void registerModEntities() {
        Bioresistance.LOGGER.info("Registering ModEntities for " + Bioresistance.MOD_ID);
    }
}