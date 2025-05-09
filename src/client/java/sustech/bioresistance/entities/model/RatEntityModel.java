package sustech.bioresistance.entities.model;

import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.entities.RatEntity;
import software.bernie.geckolib.model.GeoModel;

/**
 * 老鼠实体的模型类
 * 注意：这个类只应该在客户端环境中被使用
 */
public class RatEntityModel extends GeoModel<RatEntity> {
    @Override
    public Identifier getModelResource(RatEntity animatable) {
        return new Identifier(Bioresistance.MOD_ID, "geo/rat-converted.geo.json");
    }

    @Override
    public Identifier getTextureResource(RatEntity animatable) {
        return new Identifier(Bioresistance.MOD_ID, "textures/entity/rat/texture.png");
    }

    @Override
    public Identifier getAnimationResource(RatEntity animatable) {
        return new Identifier(Bioresistance.MOD_ID, "animations/rat-converted.animation.json");
    }
} 