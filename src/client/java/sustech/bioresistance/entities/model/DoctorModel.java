package sustech.bioresistance.entities.model;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.entities.DoctorEntity;

public class DoctorModel extends GeoModel<DoctorEntity> {
    @Override
    public Identifier getModelResource(DoctorEntity entity) {
        return new Identifier(Bioresistance.MOD_ID, "geo/doctor.geo.json");
    }

    @Override
    public Identifier getTextureResource(DoctorEntity entity) {
        return new Identifier(Bioresistance.MOD_ID, "textures/entity/doctor/doctor.png");
    }

    @Override
    public Identifier getAnimationResource(DoctorEntity entity) {
        return new Identifier(Bioresistance.MOD_ID, "animations/doctor.animation.json");
    }
} 