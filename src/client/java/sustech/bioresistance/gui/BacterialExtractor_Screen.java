package sustech.bioresistance.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.complexBlocks.BacterialExtractor_ScreenHandler;

public class BacterialExtractor_Screen extends HandledScreen<BacterialExtractor_ScreenHandler> implements ScreenHandlerProvider<BacterialExtractor_ScreenHandler> {
    // 界面贴图
    private static final Identifier TEXTURE = new Identifier(Bioresistance.MOD_ID, "textures/gui/bacterial_extractor.png");
    
    // 箭头动画的起始位置
    private static final int ARROW_X = 89;
    private static final int ARROW_Y = 40;
    
    // 模式按钮的位置和大小
    private static final int MODE1_BUTTON_X = 61;
    private static final int MODE2_BUTTON_X = 89;
    private static final int MODE_BUTTON_Y = 20;
    private static final int MODE_BUTTON_WIDTH = 29;
    private static final int MODE_BUTTON_HEIGHT = 11;
    
    // 按钮贴图在纹理上的位置
    private static final int MODE1_SELECTED_TEXTURE_X = 181;
    private static final int MODE1_SELECTED_TEXTURE_Y = 93;
    private static final int MODE1_UNSELECTED_TEXTURE_X = 215;
    private static final int MODE1_UNSELECTED_TEXTURE_Y = 93;
    private static final int MODE2_SELECTED_TEXTURE_X = 181;
    private static final int MODE2_SELECTED_TEXTURE_Y = 112;
    private static final int MODE2_UNSELECTED_TEXTURE_X = 215;
    private static final int MODE2_UNSELECTED_TEXTURE_Y = 112;

    public BacterialExtractor_Screen(BacterialExtractor_ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // 背景图贴图的宽高
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 绘制主背景
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        
        // 绘制模式1按钮
        if (handler.getSelectedMode() == 1) {
            // 选中状态
            context.drawTexture(TEXTURE, this.x + MODE1_BUTTON_X, this.y + MODE_BUTTON_Y, 
                    MODE1_SELECTED_TEXTURE_X, MODE1_SELECTED_TEXTURE_Y, 
                    MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        } else {
            // 未选中状态
            context.drawTexture(TEXTURE, this.x + MODE1_BUTTON_X, this.y + MODE_BUTTON_Y, 
                    MODE1_UNSELECTED_TEXTURE_X, MODE1_UNSELECTED_TEXTURE_Y, 
                    MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        }
        
        // 绘制模式2按钮
        if (handler.getSelectedMode() == 2) {
            // 选中状态
            context.drawTexture(TEXTURE, this.x + MODE2_BUTTON_X, this.y + MODE_BUTTON_Y, 
                    MODE2_SELECTED_TEXTURE_X, MODE2_SELECTED_TEXTURE_Y, 
                    MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        } else {
            // 未选中状态
            context.drawTexture(TEXTURE, this.x + MODE2_BUTTON_X, this.y + MODE_BUTTON_Y, 
                    MODE2_UNSELECTED_TEXTURE_X, MODE2_UNSELECTED_TEXTURE_Y, 
                    MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT);
        }
        
        // 绘制制作进度（箭头）
        int progress = handler.getCookProgress();
        if (progress > 0) {
            context.drawTexture(TEXTURE, this.x + ARROW_X, this.y + ARROW_Y, 176, 0, progress, 16);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // 居中标题
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 处理按钮点击
        if (isPointWithinBounds(MODE1_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT, mouseX, mouseY)) {
            // 点击了模式1按钮
            handler.setSelectedMode(1);
            // 发送网络包通知服务器模式已更改
            this.client.interactionManager.clickButton(this.handler.syncId, 1);
            return true;
        } else if (isPointWithinBounds(MODE2_BUTTON_X, MODE_BUTTON_Y, MODE_BUTTON_WIDTH, MODE_BUTTON_HEIGHT, mouseX, mouseY)) {
            // 点击了模式2按钮
            handler.setSelectedMode(2);
            // 发送网络包通知服务器模式已更改
            this.client.interactionManager.clickButton(this.handler.syncId, 2);
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
} 