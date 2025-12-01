package fun.rich.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;

import fun.rich.display.screens.clickgui.components.AbstractComponent;

@Setter
@Accessors(chain = true)
public class UserComponent extends AbstractComponent {
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        MatrixStack matrix = context.getMatrices();
//        Matrix4f positionMatrix = matrix.peek().getPositionMatrix();
//        DiscordManager discord = Avalora.getInstance().getDiscordManager();
//
//        rectangle.render(ShapeProperties.create(matrix, x + 5, y - 27.5f, 70, 20)
//                .round(4).color(
//                        new Color(14,14,16,255).getRGB(),
//                        new Color(38,38,48,255).getRGB(),
//                        new Color(38,38,48,255).getRGB(),
//                        new Color(14,14,16,255).getRGB()).build());
//
//        Render2DUtil.drawTexture(context, discord.getAvatarId(), x + 9, y - 24.5f, 14, 6.5F, 0, 15, 21, ColorUtil.getGuiRectColor(1));
//
////        rectangle.render(ShapeProperties.create(matrix, x + 20F, y - 15F, 5, 5)
////                .round(2.5F).color(ColorUtil.getGuiRectColor(1)).build());
////
////        rectangle.render(ShapeProperties.create(matrix, x + 21F, y - 14F, 3, 3)
////                .round(1.5F).color(0xFF26c68c).build());
//
//        ScissorManager scissor = Avalora.getInstance().getScissorManager();
//        scissor.push(positionMatrix, x + 5.5F, y - 29.5F, 74, 22);
//
//        ScissorManager scissorManager = Avalora.getInstance().getScissorManager();
//        scissorManager.push(matrix.peek().getPositionMatrix(), x + 5.5F, y - 29.5F, 74, 22);
//        Fonts.getSize(12, Fonts.Type.SEMI).drawGradientString(matrix, "Username", x + 26, y - 21f, ColorUtil.getText(), ColorUtil.getText(0.5F));
//        scissorManager.pop();
//
//        Fonts.getSize(10, Fonts.Type.SEMI).drawGradientString(matrix, StringUtil.getUserRole(), x + 26, y - 14.5f, ColorUtil.fade(0), ColorUtil.fade(300));
//        scissor.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
