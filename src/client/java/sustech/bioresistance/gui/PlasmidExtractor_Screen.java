package sustech.bioresistance.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import sustech.bioresistance.Bioresistance;
import sustech.bioresistance.complexBlocks.PlasmidExtractor_ScreenHandler;

public class PlasmidExtractor_Screen extends HandledScreen<PlasmidExtractor_ScreenHandler> implements ScreenHandlerProvider<PlasmidExtractor_ScreenHandler> {
    // 界面贴图
    private static final Identifier TEXTURE = new Identifier(Bioresistance.MOD_ID, "textures/gui/plasmid_extractor.png");
    
    // 按钮的位置和大小
    private static final int BUTTON1_X = 64;
    private static final int BUTTON1_Y = 47;
    private static final int BUTTON2_X = 168;
    private static final int BUTTON2_Y = 47;
    private static final int BUTTON_WIDTH = 29;
    private static final int BUTTON_HEIGHT = 11;
    
    // 按钮贴图在纹理上的位置
    private static final int BUTTON1_TEXTURE_X = 0;
    private static final int BUTTON1_TEXTURE_Y = 171;
    private static final int BUTTON1_PRESSED_TEXTURE_X = 0;
    private static final int BUTTON1_PRESSED_TEXTURE_Y = 187;
    private static final int BUTTON2_TEXTURE_X = 0;
    private static final int BUTTON2_TEXTURE_Y = 171;
    private static final int BUTTON2_PRESSED_TEXTURE_X = 0;
    private static final int BUTTON2_PRESSED_TEXTURE_Y = 187;

    public PlasmidExtractor_Screen(PlasmidExtractor_ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // 背景图贴图的宽高
        this.backgroundWidth = 256;
        this.backgroundHeight = 165;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 绘制主背景
        context.drawTexture(TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        
        // 绘制按钮1
        if (handler.getActiveButton() == 1) {
            // 按下状态
            context.drawTexture(TEXTURE, this.x + BUTTON1_X, this.y + BUTTON1_Y, 
                    BUTTON1_PRESSED_TEXTURE_X, BUTTON1_PRESSED_TEXTURE_Y, 
                    BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            // 未按下状态
            context.drawTexture(TEXTURE, this.x + BUTTON1_X, this.y + BUTTON1_Y, 
                    BUTTON1_TEXTURE_X, BUTTON1_TEXTURE_Y, 
                    BUTTON_WIDTH, BUTTON_HEIGHT);
        }
        
        // 绘制按钮2
        if (handler.getActiveButton() == 2) {
            // 按下状态
            context.drawTexture(TEXTURE, this.x + BUTTON2_X, this.y + BUTTON2_Y, 
                    BUTTON2_PRESSED_TEXTURE_X, BUTTON2_PRESSED_TEXTURE_Y, 
                    BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            // 未按下状态
            context.drawTexture(TEXTURE, this.x + BUTTON2_X, this.y + BUTTON2_Y, 
                    BUTTON2_TEXTURE_X, BUTTON2_TEXTURE_Y, 
                    BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        
        // 自定义渲染标题，使用两行显示
        String titleText = Text.translatable("container.plasmid_extractor").getString();
        if (titleText.contains("\n")) {
            String[] lines = titleText.split("\n");
            if (lines.length >= 2) {
                context.drawText(
                    this.textRenderer,
                    lines[0],
                    this.x + 2,
                    this.y + 85,
                    4210752,
                    false
                );
                context.drawText(
                    this.textRenderer,
                    lines[1],
                    this.x + 2,
                    this.y + 95,
                    4210752,
                    false
                );
            }
        } else {
            context.drawText(
                this.textRenderer,
                titleText,
                this.x + 5,
                this.y + 81,
                4210752,
                false
            );
        }
    }

    @Override
    protected void init() {
        super.init();
        // 设置标题位置
        titleX = -10000; // 将原标题移出可见区域，因为我们自己绘制
        titleY = -10000;
        
        // 隐藏玩家物品栏标题
        this.playerInventoryTitleY = this.height + 10; // 将物品栏标题移出可见区域
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 处理按钮点击
        if (handler.isClickInButton1((int)(mouseX - x), (int)(mouseY - y))) {
            // 点击了按钮1
            handler.setActiveButton(1);
            // 发送网络包通知服务器按钮已按下
            this.client.interactionManager.clickButton(this.handler.syncId, 1);
            return true;
        } else if (handler.isClickInButton2((int)(mouseX - x), (int)(mouseY - y))) {
            // 点击了按钮2
            handler.setActiveButton(2);
            // 发送网络包通知服务器按钮已按下
            this.client.interactionManager.clickButton(this.handler.syncId, 2);
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
} 