package fun.rich.display.screens.clickgui.components.implement.settings;

import fun.rich.Rich;
import fun.rich.utils.display.scissor.ScissorAssist;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static fun.rich.utils.display.font.Fonts.Type.*;

public class SliderComponent extends AbstractSettingComponent {
    public static final int SLIDER_WIDTH = 65;

    private final SliderSettings setting;

    private boolean dragging;
    private double animation;

    public SliderComponent(SliderSettings setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        float nameWidth = Fonts.getSize(12, DEFAULT).getStringWidth(setting.getName());

        height = 20;

        String value = String.valueOf(setting.getValue());

        Fonts.getSize(12, BOLD).drawString(matrix, value, x + width - 9 - Fonts.getSize(12).getStringWidth(value), y + 8, ColorAssist.getText());

        changeValue(getDifference(mouseX, matrix));

        Fonts.getSize(20, GUIICONS).drawString(matrix, "H", x + 6, y + 14.5f, new Color(128, 128, 128, 64).getRGB());

        float offset = 62;
        if (nameWidth > 62) {
            ScissorAssist scissor = Rich.getInstance().getScissorManager();
            scissor.push(matrix.peek().getPositionMatrix(), x + 19, y + 12f, 65, 50);
            Fonts.getSize(12, DEFAULT).drawStringWithScroll(matrix, setting.getName(), x + 19, y + 15f, offset, new Color(225, 225, 225, 225).getRGB());
            scissor.pop();

        } else {
            Fonts.getSize(12, DEFAULT).drawString(matrix, setting.getName(), x + 19, y + 15f, 0xFFD4D6E1);
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragging = Calculate.isHovered(mouseX, mouseY, x + width - SLIDER_WIDTH - 9, y + 12, SLIDER_WIDTH, 8) && button == 0;
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private float getDifference(int mouseX, MatrixStack matrix) {
        float percentValue = SLIDER_WIDTH * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()),
                difference = MathHelper.clamp(mouseX - (x + width - SLIDER_WIDTH - 9), 0, SLIDER_WIDTH);

        animation = Calculate.interpolate(animation, percentValue);

        rectangle.render(ShapeProperties.create(matrix, x + width - SLIDER_WIDTH - 9, y + 15, SLIDER_WIDTH, 3).round(1)
                .color(0x2D2E414D).build());

        rectangle.render(ShapeProperties.create(matrix, x + width - SLIDER_WIDTH - 9, y + 15, (float) animation, 3).round(1)
                .color(new Color(55, 55, 60, 155).getRGB(), new Color(155, 155, 160, 155).getRGB(), new Color(255, 255, 255, 155).getRGB(), new Color(255, 255, 250, 155).getRGB()).build());

        float v = MathHelper.clamp((float) (x + width - SLIDER_WIDTH + animation), 0, x + width - 4);
        rectangle.render(ShapeProperties.create(matrix, v - 10.5f, y + 12.5F, 7, 7)
                .round(3).color(ColorAssist.getMainGuiColor()).build());

        rectangle.render(ShapeProperties.create(matrix, v - 10F, y + 13.5F, 6, 6)
                .round(3).thickness(2).softness(0).color(new Color(61, 67, 71, 255).getRGB(),
                        new Color(71, 77, 81, 255).getRGB(),
                        new Color(81, 87, 91, 255).getRGB(),
                        new Color(91, 97, 101, 255).getRGB()).build());

        return difference;
    }


    private void changeValue(float difference) {
        BigDecimal bd = BigDecimal.valueOf((difference / SLIDER_WIDTH) * (setting.getMax() - setting.getMin()) + setting.getMin())
                .setScale(2, RoundingMode.HALF_UP);

        if (dragging) {
            float value = difference == 0 ? setting.getMin() : bd.floatValue();
            if (setting.isInteger()) value = (int) value;
            setting.setValue(value);
        }
    }
}