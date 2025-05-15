package sustech.bioresistance.entities.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import sustech.bioresistance.entities.DoctorEntity;
import sustech.bioresistance.entities.model.DoctorModel;

/**
 * 医生实体的渲染器类
 * 注意：这个类只应该在客户端环境中被使用
 */
public class DoctorRenderer extends GeoEntityRenderer<DoctorEntity> {
    public DoctorRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DoctorModel());
        // 设置阴影半径
        this.shadowRadius = 0.5f;
    }
} 