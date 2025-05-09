package sustech.bioresistance.entities.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import sustech.bioresistance.entities.RatEntity;
import sustech.bioresistance.entities.model.RatEntityModel;

/**
 * 老鼠实体的渲染器类
 * 注意：这个类只应该在客户端环境中被使用
 */
public class RatEntityRenderer extends GeoEntityRenderer<RatEntity> {
    public RatEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new RatEntityModel());
        // 设置阴影半径
        this.shadowRadius = 0.3f;
    }
    
    @Override
    protected void applyRotations(RatEntity animatable, MatrixStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        // 修正老鼠朝向问题 - 旋转-90度
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
    }
} 