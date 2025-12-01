package fun.rich.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.glfw.GLFW;

import fun.rich.features.module.setting.implement.BindSetting;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.client.chat.StringHelper;

import java.awt.*;

import static fun.rich.utils.display.font.Fonts.Type.*;

public class BindComponent extends AbstractSettingComponent {
    private final BindSetting setting;
    private boolean binding;

    public BindComponent(BindSetting setting) {
        super(setting);
        this.setting = setting;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        java.lang.String bindName = StringHelper.getBindName(setting.getKey());
        java.lang.String name = binding ? "(" + bindName + ") ..." : bindName;
        float stringWidth = Fonts.getSize(11, SEMI).getStringWidth(name) - 2;

        height = 15;

        rectangle.render(ShapeProperties.create(matrix, x + width - stringWidth - 19, y + 5.5f, stringWidth + 10, 11.5f)
                .round(3).softness(1).thickness(2).outlineColor(new Color(55,52,55,255).getRGB())
                .color(
                        new Color(25,22,25,0).getRGB(),
                        new Color(31,27,35,0).getRGB(),
                        new Color(31,27,35,0).getRGB(),
                        new Color(25,22,25,0).getRGB())
                .build());
        int bindingColor = ColorHelper.getArgb(255, 135, 136, 148);

        Fonts.getSize(11, SEMI).drawString(matrix, name, x + width - 14 - stringWidth - 1, y + 10.5f, bindingColor);

        Fonts.getSize(14, GUIICONS).drawString(context.getMatrices(), "L", x + 6, y + 11f, new Color(128, 128, 128, 64).getRGB());

        Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), setting.getName(), x + 17, y + 10f, 0xFFD4D6E1);
//        Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), wrapped, x + 9, y + 15, 0xFF878894);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (Calculate.isHovered(mouseX, mouseY, x, y, width, height)) {
                binding = !binding;
            } else {
                binding = false;
            }
        }

        if (binding && button > 1) {
            setting.setKey(button);
            binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int key = keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode;
        if (binding) {
            setting.setKey(key);
            binding = false;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
