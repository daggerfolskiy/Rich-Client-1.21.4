package fun.rich.display.screens.clickgui.components.implement.settings.multiselect;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.rich.features.module.setting.implement.MultiSelectSetting;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.components.implement.settings.select.SelectedButton;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static fun.rich.utils.display.font.Fonts.Type.BOLD;

public class MultiSelectedButton extends AbstractComponent {
    private final MultiSelectSetting setting;
    private final String text;
    @Setter
    @Accessors(chain = true)
    private float alpha;
    private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(0.5);

    public MultiSelectedButton(MultiSelectSetting setting, String text) {
        this.setting = setting;
        this.text = text;
        alphaAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        alphaAnimation.setDirection(setting.getSelected().contains(text) ? Direction.FORWARDS : Direction.BACKWARDS);
        float opacity = alphaAnimation.getOutput().floatValue();
        int adjustedAlpha = (int) Calculate.clamp(opacity * alpha * 255, 0, 255);
        int selectedOpacity = new Color(48, 48, 51, adjustedAlpha).getRGB();

        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) {
            rectangle.render(ShapeProperties.create(context.getMatrices(), x + 0.5f, y, width - 1, height - 0.5f).round
                    (SelectedButton.getRound(setting.getList(), text)).color(
                    new Color(58, 58, 60, adjustedAlpha).getRGB(),
                    new Color(58, 58, 60, adjustedAlpha).getRGB(),
                    new Color(58, 58, 60, 0).getRGB(),
                    new Color(58, 58, 60, 0).getRGB()).build());

        }

        Fonts.getSize(12, BOLD).drawString(matrix, text, x + 4, y + 5, ColorAssist.multAlpha(new Color(225, 225, 225, 225).getRGB(), Calculate.clamp(alpha, 0, 1)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            List<String> selected = new ArrayList<>(setting.getSelected());
            if (selected.contains(text)) {
                selected.remove(text);
            } else {
                selected.add(text);
                sortSelectedAccordingToList(selected, setting.getList());
            }
            setting.setSelected(selected);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sortSelectedAccordingToList(List<String> selected, List<String> list) {
        selected.sort(Comparator.comparingInt(list::indexOf));
    }
}