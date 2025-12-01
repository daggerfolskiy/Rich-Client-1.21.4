package fun.rich.display.screens.clickgui.components.implement.window.implement;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.components.implement.window.AbstractWindow;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.client.chat.StringHelper;

@RequiredArgsConstructor
public abstract class AbstractBindWindow extends AbstractWindow {
    private boolean binding;

    protected abstract int getKey();

    protected abstract void setKey(int key);

    protected abstract int getType();

    protected abstract void setType(int type);

    @Override
    public void drawWindow(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(4).softness(25).color(0x32000000).build());

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height)
                .round(4).thickness(2).outlineColor(ColorAssist.getOutline(0.8F,1)).color(ColorAssist.getRect(1)).build());

        Fonts.getSize(14).drawString(matrix, "Binding module", x + 5, y + 8, -1);

        image.setTexture("textures/trash.png").render(ShapeProperties.create(matrix, x + width - 13, y + 5.3f, 8, 8).build());

        drawKeyButton(matrix);
        drawTypeButton(matrix);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (Calculate.isHovered(mouseX, mouseY, x + width - 57, y + 37F, 52, 13)) {
                setType(getType() != 1 ? 1 : 0);
            }

            float stringWidth = Fonts.getSize(14).getStringWidth(StringHelper.getBindName(getKey()));

            if (Calculate.isHovered(mouseX, mouseY, x + width - stringWidth - 15, y + 18.8F, stringWidth + 10, 13)) {
                binding = !binding;
            }

            if (Calculate.isHovered(mouseX, mouseY, x + width - 13, y + 5.3f, 8, 8)) {
                setKey(-1);
            }
        }

        if (binding && button > 1) {
            setKey(button);
            binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int key = keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode;
        if (binding) {
            setKey(key);
            binding = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    private void drawKeyButton(MatrixStack matrix) {
        float stringWidth = Fonts.getSize(14).getStringWidth(StringHelper.getBindName(getKey()));

        rectangle.render(ShapeProperties.create(matrix, x + width - stringWidth - 15, y + 18.8F, stringWidth + 10, 13)
                .round(2).thickness(2).softness(1).outlineColor(ColorAssist.getOutline(0.8F,1)).color(ColorAssist.getOutline(0.1F,1)).build());

        int bindingColor = binding ? 0xFF8187FF : 0xFFD4D6E1;

        Fonts.getSize(14).drawString(matrix, StringHelper.getBindName(getKey()), x + width - 10 - stringWidth, y + 23.6F, bindingColor);
        Fonts.getSize(14).drawString(matrix, "Key", (int) (x + 5), (int) (y + 24.3), 0xFFD4D6E1);
    }

    private void drawTypeButton(MatrixStack matrix) {
        rectangle.render(ShapeProperties.create(matrix, x + width - 57, y + 37F, 52, 13)
                .round(2).thickness(2).softness(1).outlineColor(ColorAssist.getOutline(0.8F,1)).color(ColorAssist.getOutline(0.1F,1)).build());

        if (getType() == 1) {
            rectangle.render(ShapeProperties.create(matrix, x + width - 34, y + 37F, 29, 13)
                    .round(2, 2, 0, 0).color(0xFF8187FF).build());
        } else {
            rectangle.render(ShapeProperties.create(matrix, x + width - 57, y + 37F, 23, 13)
                    .round(0, 0, 2, 2).color(0xFF8187FF).build());
        }

        Fonts.getSize(12).drawString(matrix, "HOLD", x + 52, y + 42.3, 0xFFD4D6E1);
        Fonts.getSize(12).drawString(matrix, "TOGGLE", x + 73, y + 42.3, 0xFFD4D6E1);

        Fonts.getSize(14).drawString(matrix, "Bind mode", (int) (x + 5), (int) (y + 42.3F), 0xFFD4D6E1);
    }
}