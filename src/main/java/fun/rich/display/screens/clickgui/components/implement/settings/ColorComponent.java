package fun.rich.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.components.implement.window.AbstractWindow;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.display.screens.clickgui.components.implement.window.implement.settings.color.ColorWindow;

import java.awt.*;

import static fun.rich.utils.display.font.Fonts.Type.*;

public class ColorComponent extends AbstractSettingComponent {
    private final ColorSetting setting;

    public ColorComponent(ColorSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        height = 20;

        Fonts.getSize(16, ICONSCATEGORY).drawString(context.getMatrices(), "G", x + 6, y + 11.5f, new Color(128, 128, 128, 64).getRGB());

        Fonts.getSize(13, DEFAULT).drawString(context.getMatrices(), setting.getName(), x + 17, y + 12f, 0xFFD4D6E1);
//        Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), wrapped, x + 9, y + 15, 0xFF878894);

        rectangle.render(ShapeProperties.create(matrix, x + width - 18, y + 10, 7, 7)
                .round(3.5F).color(setting.getColor()).build());

        rectangle.render(ShapeProperties.create(matrix, x + width - 18, y + 10, 7, 7)
                .round(3.5F).thickness(2).softness(1).outlineColor(ColorAssist.getText()).color(0x0FFFFFF).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x + width - 15, y + 10, 7, 7) && button == 0) {
            AbstractWindow existingWindow = null;

            for (AbstractWindow window : windowManager.getWindows()) {
                if (window instanceof ColorWindow) {
                    existingWindow = window;
                    break;
                }
            }

            if (existingWindow != null) {
                windowManager.delete(existingWindow);
            } else {
                AbstractWindow colorWindow = new ColorWindow(setting)
                        .position((int) (mouseX - 110), (int) (mouseY- 20))
                        .size(100, 155)
                        .draggable(true);

                windowManager.add(colorWindow);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
