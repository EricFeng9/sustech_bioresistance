package sustech.bioresistance.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.complexBlocks.Bio_Fridge_ScreenHandler;



public class Bio_Fridge_Screen extends HandledScreen<Bio_Fridge_ScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Bioresistance.MOD_ID, "textures/gui/bio_fridge.png");

    public Bio_Fridge_Screen(Bio_Fridge_ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176; // 根据贴图尺寸来
        this.backgroundHeight = 166; // 同上
    }

    //初始化gui的位置
    @Override
    protected void init(){
        this.x = (this.width - backgroundWidth) / 2;
        this.y = (this.height - backgroundHeight) / 2;
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 注：加一个半透明灰色背景、模糊视野之类
        this.renderBackground(context,mouseX,mouseY,delta);

        // 2) 再让 HandledScreen 自己画槽位/物品等
        super.render(context, mouseX, mouseY, delta);
        // 3) 再画自定义的背景贴图(顺序不能错)
        this.drawBackgroundTexture(context);
        // 绘制文本
        context.drawText(
                this.textRenderer,
                Text.translatable("container.bio_fridge").getString(),
                this.x+8,
                this.y+5,
                4210752,
                false
        );
        // 4) 画鼠标悬停提示
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

    }

    private void drawBackgroundTexture(DrawContext context) {
        // 贴图左上角渲染到 screen 的 (this.x, this.y)
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0,
                this.backgroundWidth, this.backgroundHeight);
    }

    // 如果你想在物品栏上方显示文字，也可以 override drawForeground(...)、drawTitle(...) 等


}
