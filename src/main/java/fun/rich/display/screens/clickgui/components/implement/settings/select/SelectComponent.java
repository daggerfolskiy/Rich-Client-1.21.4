package fun.rich.display.screens.clickgui.components.implement.settings.select;

import fun.rich.utils.display.font.FontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import fun.rich.utils.math.calc.Calculate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static fun.rich.utils.display.font.Fonts.Type.*;

public class SelectComponent extends AbstractSettingComponent {
    private final List<SelectedButton> selectedButtons = new ArrayList<>();
    private final SelectSetting setting;
    private boolean open;
    private float dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight;
    private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(1);
    private final Animation heightAnimation = new Decelerate().setMs(200).setValue(0);

    public SelectComponent(SelectSetting setting) {
        super(setting);
        this.setting = setting;
        alphaAnimation.setDirection(Direction.BACKWARDS);
        heightAnimation.setDirection(Direction.BACKWARDS);
        for (String s : setting.getList()) {
            selectedButtons.add(new SelectedButton(setting, s));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        float baseHeight = 20;
        List<String> fullSettingsList = setting.getList();
        this.dropdownListX = x + width - 75;
        this.dropDownListY = y + 23;
        this.dropDownListWidth = 66;
        this.dropDownListHeight = fullSettingsList.size() * 12;
        if (open) {
            alphaAnimation.setDirection(Direction.FORWARDS);
            heightAnimation.setDirection(Direction.FORWARDS);
            heightAnimation.setValue(dropDownListHeight);
        } else {
            alphaAnimation.setDirection(Direction.BACKWARDS);
            heightAnimation.setDirection(Direction.BACKWARDS);
        }
        height = (int) (baseHeight + heightAnimation.getOutput().floatValue() + (open ? 5 : 0));
        renderSelected(matrices);
        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) renderSelectList(context, mouseX, mouseY, delta);
        Fonts.getSize(21, GUIICONS).drawString(matrices, "J", x + 6f, y + 11f, new Color(128, 128, 128, 64).getRGB());
        Fonts.getSize(12, DEFAULT).drawString(matrices, setting.getName(), x + 19, y + 13f, 0xFFD4D6E1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (Calculate.isHovered(mouseX, mouseY, x + width - 75, y + 4, 66, 17)) {
                open = !open;
                return true;
            } else if (open && !isHoveredList(mouseX, mouseY)) {
                open = false;
                return true;
            }
            if (open) {
                for (SelectedButton selectedButton : selectedButtons) {
                    if (selectedButton.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return open && isHoveredList(mouseX, mouseY);
    }

    private void renderSelected(MatrixStack matrices) {
        FontRenderer font = Fonts.getSize(12);
        int x1 = (int) (x + width - 72);
        float offset = 64;
        rectangle.render(ShapeProperties.create(matrices, x + width - 75, y + 7, 66, 14)
                .round(3).thickness(2).outlineColor(new Color(35, 52, 55, 155).getRGB())
                .color(
                        new Color(15, 15, 15, 0).getRGB(),
                        new Color(15, 15, 15, 0).getRGB(),
                        new Color(15, 15, 15, 0).getRGB(),
                        new Color(15, 15, 15, 0).getRGB())
                .build());
        String selectedName = String.join(", ", setting.getSelected());
        Fonts.getSize(12, BOLD).drawString(matrices, selectedName, x + width - 75 + 3, y + 13, new Color(225, 225, 225, 225).getRGB());

    }

    private void renderSelectList(DrawContext context, int mouseX, int mouseY, float delta) {
        float opacity = alphaAnimation.getOutput().floatValue();
        int alpha = (int) (opacity * 0);
        float animatedHeight = heightAnimation.getOutput().floatValue();
        rectangle.render(ShapeProperties.create(context.getMatrices(), dropdownListX, dropDownListY, dropDownListWidth, animatedHeight)
                .round(3).thickness(2).outlineColor(new Color(55, 52, 55, 155).getRGB())
                .color(
                        new Color(15, 15, 15, alpha).getRGB(),
                        new Color(15, 15, 15, alpha).getRGB(),
                        new Color(15, 15, 15, alpha).getRGB(),
                        new Color(15, 15, 15, alpha).getRGB())
                .build());
        float offset = dropDownListY;
        for (SelectedButton button : selectedButtons) {
            button.x = dropdownListX;
            button.y = offset;
            button.width = dropDownListWidth;
            button.height = 12;
            button.setAlpha(opacity);
            if (offset - dropDownListY < animatedHeight) {
                button.render(context, mouseX, mouseY, delta);
            }
            offset += 12;
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return Calculate.isHovered(mouseX, mouseY, dropdownListX, dropDownListY - 16, dropDownListWidth, dropDownListHeight + 16);
    }
}