package sustech.bioresistance.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.complexBlocks.Autoclave_ScreenHandler;

// AutoclaveScreen.java
public class Autoclave_Screen extends HandledScreen<Autoclave_ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Bioresistance.MOD_ID, "textures/gui/autoclave.png");

    public Autoclave_Screen(Autoclave_ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        // 燃烧进度（火焰图标，垂直方向，保持原样）
        int burnProgress = handler.getBurnProgress();
        context.drawTexture(TEXTURE,
                x + 99, y + 77 - burnProgress, // 坐标
                178, 35 - burnProgress,             // 纹理偏移
                22, 20/*burnProgress + 1*/               // 宽度和高度
        );
        // 绘制箭头进度
        // 制作进度（箭头，水平从左到右）
        int cookProgress = handler.getCookProgress();
        context.drawTexture(TEXTURE,
                x + 72, y + 38,       // 起始坐标
                176, 3,              // 纹理起始位置
                cookProgress, 7       // 进度宽度和固定高度
        );
        this.titleY=-100000;//把原版标题移到屏幕外
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        // 绘制文本
        context.drawText(
                this.textRenderer,
                Text.translatable("container.autoclave").getString(),
                this.x+8,
                this.y+5,
                4210752,
                false
        );
    }
}