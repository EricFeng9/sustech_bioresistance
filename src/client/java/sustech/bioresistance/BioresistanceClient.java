package sustech.bioresistance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.util.Identifier;
import sustech.bioresistance.entities.RatEntity;
import sustech.bioresistance.gui.Autoclave_Screen;
import sustech.bioresistance.gui.BacterialExtractor_Screen;
import sustech.bioresistance.gui.Bio_Fridge_Screen;
import sustech.bioresistance.gui.CleanTable_Screen;
import sustech.bioresistance.gui.PlasmidExtractor_Screen;
import sustech.bioresistance.entities.renderer.RatEntityRenderer;

/**
 * 客户端初始化类，只在客户端环境中运行
 */
public class BioresistanceClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ScreenRegistry.register(
				ModScreenHandlers.BIO_FRIDGE_SCREEN_HANDLER,
                Bio_Fridge_Screen::new
		);
		ScreenRegistry.register(
				ModScreenHandlers.AUTOCLAVE_SCREEN_HANDLER,
                Autoclave_Screen::new
		);

		ScreenRegistry.register(
				ModScreenHandlers.CLEAN_TABLE_SCREEN_HANDLER,
                CleanTable_Screen::new
		);

		ScreenRegistry.register(
				ModScreenHandlers.BACTERIAL_EXTRACTOR_SCREEN_HANDLER,
				BacterialExtractor_Screen::new
		);
		
		ScreenRegistry.register(
				ModScreenHandlers.PLASMID_EXTRACTOR_SCREEN_HANDLER,
				PlasmidExtractor_Screen::new
		);

		// 注册油流体渲染
		FluidRenderHandlerRegistry.INSTANCE.register(
				ModFluids.STILL_OIL,
				ModFluids.FLOWING_OIL,
				new SimpleFluidRenderHandler(
						new Identifier(Bioresistance.MOD_ID+":block/still_oil"),
						new Identifier(Bioresistance.MOD_ID+":block/flowing_oil"),
						0xFFFFFF // RGB颜色（深灰色）
				)
		);

		// 注册土壤浸取液流体渲染，使用水的贴图但添加棕色
		FluidRenderHandlerRegistry.INSTANCE.register(
				ModFluids.STILL_SOIL_EXTRACT,
				ModFluids.FLOWING_SOIL_EXTRACT,
				new SimpleFluidRenderHandler(
						new Identifier("minecraft:block/water_still"),
						new Identifier("minecraft:block/water_flow"),
						0x8B4513 // RGB颜色（棕色）
				)
		);

		// 将粘液块绑定到半透明渲染层（根据需求选择 TRANSLUCENT 或 CUTOUT）
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.Agar_Block_Yellow,
				RenderLayer.getTranslucent()
		);
		
		// 将土壤浸取液流体方块和流体都绑定到透明渲染层
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModFluids.SOIL_EXTRACT_FLUID_BLOCK,
				RenderLayer.getTranslucent()
		);
		BlockRenderLayerMap.INSTANCE.putFluid(
				ModFluids.STILL_SOIL_EXTRACT,
				RenderLayer.getTranslucent()
		);
		BlockRenderLayerMap.INSTANCE.putFluid(
				ModFluids.FLOWING_SOIL_EXTRACT,
				RenderLayer.getTranslucent()
		);
		
		// 注册老鼠实体的渲染器
		EntityRendererRegistry.register(ModEntities.RAT, RatEntityRenderer::new);
	}
}